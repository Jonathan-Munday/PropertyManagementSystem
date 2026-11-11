package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.service.BusinessCalendar;
import za.ac.mycput.infrastructure.billing.BillingProviderStatus;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.adapter.BillingInvoiceJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationSubscriptionJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.PropertyJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.SubscriptionPlanJpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.BillingInvoiceJpaEntity;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationSubscriptionJpaEntity;
import za.ac.mycput.infrastructure.persistence.entity.SubscriptionPlanJpaEntity;
import za.ac.mycput.infrastructure.security.OrganisationContext;
import za.ac.mycput.interfaces.rest.dto.PlanResponse;
import za.ac.mycput.interfaces.rest.dto.SubscriptionHistoryItem;
import za.ac.mycput.interfaces.rest.dto.SubscriptionResponse;
import za.ac.mycput.interfaces.rest.dto.UsageResponse;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Organisation software subscription. Rent payments are not written here.
 * Without a verified payment webhook, a package change is stored as a demo subscription.
 */
@Service
public class SubscriptionApplicationService {

    private static final List<String> OPEN = List.of("TRIALING", "ACTIVE", "PAST_DUE", "DEMO");

    private final OrganisationSubscriptionJpaRepository subscriptions;
    private final SubscriptionPlanJpaRepository plans;
    private final BillingInvoiceJpaRepository invoices;
    private final PropertyJpaRepository properties;
    private final ScopedPersistence persistence;
    private final SubscriptionAllowance allowance;
    private final BillingProviderStatus providers;
    private final Clock clock;

    public SubscriptionApplicationService(OrganisationSubscriptionJpaRepository subscriptions,
                                          SubscriptionPlanJpaRepository plans,
                                          BillingInvoiceJpaRepository invoices,
                                          PropertyJpaRepository properties,
                                          ScopedPersistence persistence,
                                          SubscriptionAllowance allowance,
                                          BillingProviderStatus providers,
                                          Clock clock) {
        this.subscriptions = subscriptions;
        this.plans = plans;
        this.invoices = invoices;
        this.properties = properties;
        this.persistence = persistence;
        this.allowance = allowance;
        this.providers = providers;
        this.clock = clock;
    }

    @Transactional
    public SubscriptionResponse current() {
        persistence.bind();
        return view(OrganisationContext.require().organisationId());
    }

    @Transactional
    public List<PlanResponse> plans() {
        persistence.bind();
        return plans.findAllByOrderByAmountAsc().stream().map(SubscriptionApplicationService::plan).toList();
    }

    @Transactional
    public SubscriptionResponse changePlan(String planCode, String outcome) {
        persistence.bind();
        UUID organisationId = OrganisationContext.require().organisationId();
        SubscriptionPlanJpaEntity plan = plans.findByCode(normalise(planCode))
                .orElseThrow(() -> new DomainRuleException("Choose Starter, Growth, Professional, or Agency."));
        String result = checkoutOutcome(outcome);
        if (!"SUCCESS".equals(result)) {
            throw new DomainRuleException(simulatedFailure(result));
        }
        if (providers.liveChargesEnabled()) {
            throw new DomainRuleException("Live billing is not available from this server.");
        }
        allowance.lockOrganisation(organisationId);
        OrganisationSubscriptionJpaEntity row = subscriptions
                .findFirstByOrganisationIdAndStatusIn(organisationId, OPEN)
                .orElse(null);
        if (row != null && !"DEMO".equals(row.getStatus())) {
            throw new DomainRuleException(
                    "This subscription is not a demo entitlement. Simulated checkout did not change it.");
        }
        allowance.assertFits(organisationId, plan);
        if (row == null) {
            row = newSubscription(organisationId);
        }
        Instant now = Instant.now();
        row.setPlanId(plan.getId());
        row.setStatus("DEMO");
        row.setProvider("demo");
        row.setUpdatedAt(now);
        OrganisationSubscriptionJpaEntity saved = subscriptions.save(row);
        invoices.save(demoInvoice(organisationId, saved.getId(), plan));
        return view(organisationId);
    }

    @Transactional
    public void removeForDeletedOrganisation(UUID organisationId) {
        invoices.deleteByOrganisationId(organisationId);
        subscriptions.deleteByOrganisationId(organisationId);
    }

    @Transactional
    public SubscriptionResponse cancel() {
        persistence.bind();
        UUID organisationId = OrganisationContext.require().organisationId();
        OrganisationSubscriptionJpaEntity row = subscriptions
                .findFirstByOrganisationIdAndStatusIn(organisationId, OPEN)
                .orElseThrow(() -> new DomainRuleException("There is no subscription to cancel."));
        row.setStatus("CANCELED");
        row.setUpdatedAt(Instant.now());
        subscriptions.save(row);
        return view(organisationId);
    }

    private SubscriptionResponse view(UUID organisationId) {
        long propertyCount = properties.countByOrganisationId(organisationId);
        long userCount = allowance.occupiedMemberCount(organisationId);
        Optional<OrganisationSubscriptionJpaEntity> open = subscriptions
                .findFirstByOrganisationIdAndStatusIn(organisationId, OPEN);
        List<SubscriptionHistoryItem> history = invoices.findByOrganisationIdOrderByIssuedOnDesc(organisationId).stream()
                .map(SubscriptionApplicationService::history)
                .toList();
        boolean demoCheckout = !providers.liveChargesEnabled();
        if (open.isEmpty()) {
            boolean canceled = subscriptions.existsByOrganisationIdAndStatus(organisationId, "CANCELED");
            if (canceled) {
                String provider = subscriptions
                        .findFirstByOrganisationIdAndStatusIn(organisationId, List.of("CANCELED"))
                        .map(OrganisationSubscriptionJpaEntity::getProvider)
                        .filter(value -> value != null && !value.isBlank())
                        .orElse("none");
                return new SubscriptionResponse(
                        provider,
                        "CANCELED",
                        "This subscription is canceled. The portfolio can still be viewed. Choose a package before adding properties or members.",
                        "CANCELED",
                        demoCheckout,
                        providers.credentialsPresent(),
                        false,
                        null,
                        new UsageResponse(propertyCount, userCount, null, null),
                        history);
            }
            SubscriptionPlanJpaEntity starter = plans.findByCode("STARTER")
                    .orElseThrow(() -> new DomainRuleException("The Starter package is missing."));
            return new SubscriptionResponse(
                    "demo",
                    "DEMO",
                    "Starter demo entitlement. No payment was collected and this is not a paid subscription.",
                    "DEMO",
                    demoCheckout,
                    providers.credentialsPresent(),
                    false,
                    plan(starter),
                    new UsageResponse(propertyCount, userCount, starter.getMaxProperties(), starter.getMaxUsers()),
                    history);
        }
        OrganisationSubscriptionJpaEntity subscription = open.get();
        SubscriptionPlanJpaEntity plan = plans.findById(subscription.getPlanId())
                .orElseThrow(() -> new DomainRuleException("The subscription package is missing."));
        boolean demo = "DEMO".equals(subscription.getStatus()) || "demo".equalsIgnoreCase(subscription.getProvider());
        String mode = demo ? "DEMO" : subscription.getStatus();
        String detail = demo
                ? "Demo checkout only. No card was collected and this is not a paid subscription."
                : "The organisation subscription is stored here. No payment provider was called.";
        return new SubscriptionResponse(
                demo ? "demo" : subscription.getProvider(),
                demo ? "DEMO" : subscription.getStatus(),
                detail,
                mode,
                demoCheckout,
                providers.credentialsPresent(),
                false,
                plan(plan),
                new UsageResponse(propertyCount, userCount, plan.getMaxProperties(), plan.getMaxUsers()),
                history);
    }

    private OrganisationSubscriptionJpaEntity newSubscription(UUID organisationId) {
        Instant now = Instant.now();
        OrganisationSubscriptionJpaEntity row = new OrganisationSubscriptionJpaEntity();
        row.setId(UUID.randomUUID());
        row.setOrganisationId(organisationId);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return row;
    }

    private BillingInvoiceJpaEntity demoInvoice(UUID organisationId, UUID subscriptionId, SubscriptionPlanJpaEntity plan) {
        Instant now = Instant.now();
        BillingInvoiceJpaEntity invoice = new BillingInvoiceJpaEntity();
        invoice.setId(UUID.randomUUID());
        invoice.setOrganisationId(organisationId);
        invoice.setSubscriptionId(subscriptionId);
        invoice.setAmount(plan.getAmount());
        invoice.setCurrency(plan.getCurrency());
        invoice.setStatus("DEMO");
        invoice.setProvider("demo");
        invoice.setPlanCode(plan.getCode());
        invoice.setIssuedOn(BusinessCalendar.today(clock));
        invoice.setCreatedAt(now);
        invoice.setUpdatedAt(now);
        return invoice;
    }

    private static SubscriptionHistoryItem history(BillingInvoiceJpaEntity invoice) {
        String provider = invoice.getProvider() == null || invoice.getProvider().isBlank()
                ? "none"
                : invoice.getProvider();
        boolean simulated = "DEMO".equals(invoice.getStatus()) || "demo".equalsIgnoreCase(provider);
        String detail = simulated
                ? "Demo checkout. No payment was collected."
                : "Billing record " + invoice.getStatus() + ".";
        return new SubscriptionHistoryItem(
                invoice.getPlanCode(),
                invoice.getStatus(),
                provider,
                invoice.getAmount(),
                invoice.getIssuedOn(),
                detail);
    }

    private static PlanResponse plan(SubscriptionPlanJpaEntity plan) {
        return new PlanResponse(
                plan.getCode(),
                plan.getName(),
                plan.getAmount(),
                plan.getCurrency(),
                plan.getIntervalUnit(),
                plan.getMaxProperties(),
                plan.getMaxUsers());
    }

    private static String checkoutOutcome(String outcome) {
        if (outcome == null || outcome.isBlank()) {
            return "SUCCESS";
        }
        String normalised = outcome.trim().toUpperCase(Locale.ROOT);
        if ("SUCCESS".equals(normalised)) {
            return "SUCCESS";
        }
        if ("CANCELED".equals(normalised) || "CANCELLED".equals(normalised)) {
            return "CANCELED";
        }
        if ("FAILED".equals(normalised)) {
            return "FAILED";
        }
        throw new DomainRuleException("Simulated checkout must succeed, be canceled, or fail.");
    }

    private static String simulatedFailure(String outcome) {
        if ("CANCELED".equals(outcome)) {
            return "Simulated checkout was canceled. No package was activated and no payment was recorded.";
        }
        return "Simulated checkout failed. No package was activated and no payment was recorded.";
    }

    private static String normalise(String planCode) {
        if (planCode == null || planCode.isBlank()) {
            throw new DomainRuleException("Choose a package.");
        }
        return planCode.trim().toUpperCase(Locale.ROOT);
    }
}
