package za.ac.mycput.interfaces.rest.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.ac.mycput.application.service.SubscriptionApplicationService;
import za.ac.mycput.interfaces.rest.dto.ChangeSubscriptionRequest;
import za.ac.mycput.interfaces.rest.dto.PlanResponse;
import za.ac.mycput.interfaces.rest.dto.SubscriptionResponse;

import java.util.List;

/**
 * Software subscription for the selected organisation.
 * Rent collection stays on {@code /api/payment}.
 */
@RestController
@RequestMapping("/api/billing")
public class SubscriptionController {

    private final SubscriptionApplicationService subscriptions;

    public SubscriptionController(SubscriptionApplicationService subscriptions) {
        this.subscriptions = subscriptions;
    }

    @GetMapping("/plans")
    public List<PlanResponse> plans() {
        return subscriptions.plans();
    }

    @GetMapping("/subscription")
    public SubscriptionResponse current() {
        return subscriptions.current();
    }

    @PostMapping("/subscription")
    public SubscriptionResponse change(@Valid @RequestBody ChangeSubscriptionRequest request) {
        return subscriptions.changePlan(request.planCode(), request.outcome());
    }

    @PostMapping("/subscription/cancel")
    public SubscriptionResponse cancel() {
        return subscriptions.cancel();
    }
}
