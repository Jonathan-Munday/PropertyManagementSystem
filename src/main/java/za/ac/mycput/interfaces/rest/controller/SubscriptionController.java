package za.ac.mycput.interfaces.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.ac.mycput.application.port.SubscriptionStatus;
import za.ac.mycput.application.service.SubscriptionQueryService;
import za.ac.mycput.interfaces.rest.dto.SubscriptionResponse;

@RestController
@RequestMapping("/api/billing/subscription")
public class SubscriptionController {

    private final SubscriptionQueryService subscriptions;

    public SubscriptionController(SubscriptionQueryService subscriptions) {
        this.subscriptions = subscriptions;
    }

    @GetMapping
    public SubscriptionResponse current() {
        SubscriptionStatus status = subscriptions.currentStatus();
        return new SubscriptionResponse(status.provider(), status.status(), status.detail());
    }
}
