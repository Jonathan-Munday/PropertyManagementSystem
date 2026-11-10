package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.port.SubscriptionBillingPort;
import za.ac.mycput.application.port.SubscriptionStatus;

@Service
public class SubscriptionQueryService {

    private final SubscriptionBillingPort billing;

    public SubscriptionQueryService(SubscriptionBillingPort billing) {
        this.billing = billing;
    }

    @Transactional
    public SubscriptionStatus currentStatus() {
        return billing.currentStatus();
    }
}
