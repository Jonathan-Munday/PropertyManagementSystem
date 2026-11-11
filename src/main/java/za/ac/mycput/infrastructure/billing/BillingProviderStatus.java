package za.ac.mycput.infrastructure.billing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Reports whether payment-provider credentials are present.
 * A credential does not verify a webhook and does not enable a live charge.
 */
@Component
public class BillingProviderStatus {

    private final boolean credentialsPresent;

    public BillingProviderStatus(
            @Value("${portico.billing.payfast.merchant-id:}") String payfastMerchantId,
            @Value("${portico.billing.payfast.merchant-key:}") String payfastMerchantKey,
            @Value("${portico.billing.paystack.secret-key:}") String paystackSecretKey) {
        this.credentialsPresent = (present(payfastMerchantId) && present(payfastMerchantKey)) || present(paystackSecretKey);
    }

    public boolean credentialsPresent() {
        return credentialsPresent;
    }

    public boolean liveChargesEnabled() {
        return false;
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }
}
