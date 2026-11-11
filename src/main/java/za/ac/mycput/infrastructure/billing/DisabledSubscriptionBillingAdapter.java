package za.ac.mycput.infrastructure.billing;

import za.ac.mycput.application.port.SubscriptionBillingPort;
import za.ac.mycput.application.port.SubscriptionStatus;

/**
 * Kept as a reference for the unconfigured subscription response.
 * {@link PersistentSubscriptionBillingAdapter} is the running adapter.
 */
public class DisabledSubscriptionBillingAdapter implements SubscriptionBillingPort {

    @Override
    public SubscriptionStatus currentStatus() {
        return new SubscriptionStatus(
                "none",
                "NOT_CONFIGURED",
                "Software subscription billing is not connected. Rent stays on the payment resource.");
    }

    @Override
    public boolean liveChargesEnabled() {
        return false;
    }
}
