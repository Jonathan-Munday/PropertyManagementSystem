package za.ac.mycput.domain.service;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.exception.DomainRuleException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionLimitPolicyTest {

    private final SubscriptionLimitPolicy policy = new SubscriptionLimitPolicy();

    @Test
    void addingStopsAtTheCap() {
        assertDoesNotThrow(() -> policy.assertFits("properties", 4, 5, true));
        DomainRuleException error = assertThrows(DomainRuleException.class,
                () -> policy.assertFits("properties", 5, 5, true));
        assertTrue(error.getMessage().contains("5"));
        assertTrue(error.getMessage().contains("properties"));
    }

    @Test
    void aSmallerPackageCannotBeSelectedWhileUsageExceedsIt() {
        assertDoesNotThrow(() -> policy.assertFits("members", 2, 8, false));
        DomainRuleException error = assertThrows(DomainRuleException.class,
                () -> policy.assertFits("members", 6, 2, false));
        assertTrue(error.getMessage().contains("before changing package"));
    }
}
