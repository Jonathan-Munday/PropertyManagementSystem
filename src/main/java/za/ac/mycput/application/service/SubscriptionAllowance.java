package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.service.SubscriptionLimitPolicy;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationInvitationJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationMembershipJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationSubscriptionJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.PropertyJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.SubscriptionPlanJpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationSubscriptionJpaEntity;
import za.ac.mycput.infrastructure.persistence.entity.SubscriptionPlanJpaEntity;
import za.ac.mycput.infrastructure.security.OrganisationContext;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Applies a stored package cap to new properties and members.
 * Callers already own the database transaction. This service locks the organisation row
 * before the count and does not commit that transaction.
 * An organisation with no subscription row uses the Starter catalogue cap.
 * A canceled subscription blocks further additions until a package is chosen again.
 */
@Service
public class SubscriptionAllowance {

    static final String STARTER = "STARTER";
    private static final List<String> OPEN = List.of("TRIALING", "ACTIVE", "PAST_DUE", "DEMO");

    private final OrganisationJpaRepository organisations;
    private final OrganisationSubscriptionJpaRepository subscriptions;
    private final SubscriptionPlanJpaRepository plans;
    private final PropertyJpaRepository properties;
    private final OrganisationMembershipJpaRepository memberships;
    private final OrganisationInvitationJpaRepository invitations;
    private final SubscriptionLimitPolicy limits;

    public SubscriptionAllowance(OrganisationJpaRepository organisations,
                                 OrganisationSubscriptionJpaRepository subscriptions,
                                 SubscriptionPlanJpaRepository plans,
                                 PropertyJpaRepository properties,
                                 OrganisationMembershipJpaRepository memberships,
                                 OrganisationInvitationJpaRepository invitations,
                                 SubscriptionLimitPolicy limits) {
        this.organisations = organisations;
        this.subscriptions = subscriptions;
        this.plans = plans;
        this.properties = properties;
        this.memberships = memberships;
        this.invitations = invitations;
        this.limits = limits;
    }

    public void lockOrganisation(UUID organisationId) {
        if (organisations.lockById(organisationId).isEmpty()) {
            throw new DomainRuleException("The organisation is missing.");
        }
    }

    public void grantStarterDemo(UUID organisationId) {
        lockOrganisation(organisationId);
        if (subscriptions.existsByOrganisationId(organisationId)) {
            return;
        }
        SubscriptionPlanJpaEntity starter = starterPlan();
        Instant now = Instant.now();
        OrganisationSubscriptionJpaEntity row = new OrganisationSubscriptionJpaEntity();
        row.setId(UUID.randomUUID());
        row.setOrganisationId(organisationId);
        row.setPlanId(starter.getId());
        row.setStatus("DEMO");
        row.setProvider("demo");
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        subscriptions.save(row);
    }

    public void assertCanAddProperty() {
        UUID organisationId = OrganisationContext.require().organisationId();
        lockOrganisation(organisationId);
        SubscriptionPlanJpaEntity plan = enforceablePlan(organisationId);
        limits.assertFits("properties", properties.countByOrganisationId(organisationId), plan.getMaxProperties(), true);
    }

    public void assertCanAddMember() {
        UUID organisationId = OrganisationContext.require().organisationId();
        lockOrganisation(organisationId);
        SubscriptionPlanJpaEntity plan = enforceablePlan(organisationId);
        limits.assertFits("members", occupiedMemberCount(organisationId), plan.getMaxUsers(), true);
    }

    public void assertCanAcceptMember(UUID organisationId) {
        lockOrganisation(organisationId);
        SubscriptionPlanJpaEntity plan = enforceablePlan(organisationId);
        long active = memberships.countByOrganisationIdAndStatus(organisationId, "ACTIVE");
        long pending = pendingInvitations(organisationId);
        long remainingPending = Math.max(0, pending - 1);
        long occupiedAfter = active + 1 + remainingPending;
        if (occupiedAfter > plan.getMaxUsers()) {
            limits.assertFits("members", occupiedAfter - 1, plan.getMaxUsers(), true);
        }
    }

    public void assertFits(UUID organisationId, SubscriptionPlanJpaEntity plan) {
        lockOrganisation(organisationId);
        limits.assertFits("properties", properties.countByOrganisationId(organisationId), plan.getMaxProperties(), false);
        limits.assertFits("members", occupiedMemberCount(organisationId), plan.getMaxUsers(), false);
    }

    public long occupiedMemberCount(UUID organisationId) {
        return memberships.countByOrganisationIdAndStatus(organisationId, "ACTIVE") + pendingInvitations(organisationId);
    }

    public Optional<SubscriptionPlanJpaEntity> openPlan(UUID organisationId) {
        return subscriptions.findFirstByOrganisationIdAndStatusIn(organisationId, OPEN)
                .flatMap(row -> plans.findById(row.getPlanId()));
    }

    public boolean canceledWithoutOpenPlan(UUID organisationId) {
        return subscriptions.findFirstByOrganisationIdAndStatusIn(organisationId, OPEN).isEmpty()
                && subscriptions.existsByOrganisationIdAndStatus(organisationId, "CANCELED");
    }

    private SubscriptionPlanJpaEntity enforceablePlan(UUID organisationId) {
        Optional<OrganisationSubscriptionJpaEntity> open = subscriptions
                .findFirstByOrganisationIdAndStatusIn(organisationId, OPEN);
        if (open.isPresent()) {
            return plans.findById(open.get().getPlanId())
                    .orElseThrow(() -> new DomainRuleException("The subscription package is missing."));
        }
        if (subscriptions.existsByOrganisationIdAndStatus(organisationId, "CANCELED")) {
            throw new DomainRuleException(
                    "Choose a package before adding to this organisation. The previous subscription is canceled.");
        }
        return starterPlan();
    }

    private long pendingInvitations(UUID organisationId) {
        return invitations.countByOrganisationIdAndStatusAndExpiresAtAfter(
                organisationId, "PENDING", Instant.now());
    }

    private SubscriptionPlanJpaEntity starterPlan() {
        return plans.findByCode(STARTER)
                .orElseThrow(() -> new DomainRuleException("The Starter package is missing."));
    }
}
