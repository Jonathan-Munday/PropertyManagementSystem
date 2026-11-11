package za.ac.mycput.application.port;

/**
 * Outbound port for software-subscription billing.
 * Rent payments stay on the Payment aggregate and must not be sent through this port.
 */
public interface SubscriptionBillingPort {

    SubscriptionStatus currentStatus();

    /**
     * True only after a verified payment webhook has been implemented.
     * Stored provider credentials do not make this true.
     */
    boolean liveChargesEnabled();
}
