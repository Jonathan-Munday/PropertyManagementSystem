package za.ac.mycput.infrastructure.billing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingProviderStatusTest {

    @Test
    void credentialsDoNotTurnOnALiveCharge() {
        BillingProviderStatus configured = new BillingProviderStatus("merchant", "key", "");
        assertTrue(configured.credentialsPresent());
        assertFalse(configured.liveChargesEnabled());

        BillingProviderStatus empty = new BillingProviderStatus("", "", "");
        assertFalse(empty.credentialsPresent());
        assertFalse(empty.liveChargesEnabled());
    }
}
