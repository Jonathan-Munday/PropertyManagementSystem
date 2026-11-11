package za.ac.mycput.infrastructure.billing;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.port.SubscriptionBillingPort;
import za.ac.mycput.application.port.SubscriptionStatus;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationSubscriptionJpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationSubscriptionJpaEntity;
import za.ac.mycput.infrastructure.security.OrganisationContext;

import java.util.List;
import java.util.UUID;

/**
 * Reads the organisation subscription stored in PostgreSQL.
 * No payment provider is called. Rent collection stays on the payment resource.
 */
@Component
public class PersistentSubscriptionBillingAdapter implements SubscriptionBillingPort {

    private static final List<String> OPEN = List.of("TRIALING", "ACTIVE", "PAST_DUE", "DEMO");

    private final OrganisationSubscriptionJpaRepository subscriptions;
    private final ScopedPersistence persistence;

    public PersistentSubscriptionBillingAdapter(OrganisationSubscriptionJpaRepository subscriptions,
                                                ScopedPersistence persistence) {
        this.subscriptions = subscriptions;
        this.persistence = persistence;
    }

    @Override
    public SubscriptionStatus currentStatus() {
        persistence.bind();
        return subscriptions
                .findFirstByOrganisationIdAndStatusIn(OrganisationContext.require().organisationId(), OPEN)
                .map(PersistentSubscriptionBillingAdapter::toStatus)
                .orElseGet(() -> canceled(OrganisationContext.require().organisationId()));
    }

    private SubscriptionStatus canceled(UUID organisationId) {
        if (subscriptions.existsByOrganisationIdAndStatus(organisationId, "CANCELED")) {
            return new SubscriptionStatus(
                    "demo",
                    "CANCELED",
                    "This subscription is canceled. Choose a package before adding properties or members.");
        }
        return new SubscriptionStatus(
                "demo",
                "DEMO",
                "Starter demo entitlement. No payment was collected and this is not a paid subscription.");
    }

    private static SubscriptionStatus toStatus(OrganisationSubscriptionJpaEntity subscription) {
        if ("DEMO".equals(subscription.getStatus()) || "demo".equalsIgnoreCase(subscription.getProvider())) {
            return new SubscriptionStatus(
                    "demo",
                    "DEMO",
                    "Demo checkout only. No payment was collected and this is not a paid subscription.");
        }
        return new SubscriptionStatus(
                subscription.getProvider(),
                subscription.getStatus(),
                "The organisation subscription is stored in PostgreSQL. No provider was called.");
    }

    @Override
    public boolean liveChargesEnabled() {
        return false;
    }
}
