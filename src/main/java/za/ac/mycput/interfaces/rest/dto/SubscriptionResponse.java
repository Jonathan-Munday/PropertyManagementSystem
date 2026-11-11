package za.ac.mycput.interfaces.rest.dto;

import java.util.List;

/**
 * Software subscription for the organisation. This is not a rent payment.
 * {@code provider} and {@code status} stay at the front.
 * An organisation with no subscription row is reported as the Starter demo entitlement.
 */
public record SubscriptionResponse(
        String provider,
        String status,
        String detail,
        String billingMode,
        boolean demoCheckout,
        boolean credentialsPresent,
        boolean liveChargesEnabled,
        PlanResponse plan,
        UsageResponse usage,
        List<SubscriptionHistoryItem> history
) {
}
