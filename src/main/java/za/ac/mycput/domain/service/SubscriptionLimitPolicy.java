package za.ac.mycput.domain.service;

import za.ac.mycput.domain.exception.DomainRuleException;

/**
 * Compares organisation usage with a package cap.
 * Callers supply the counts. This policy does not load the database.
 */
public final class SubscriptionLimitPolicy {

    public void assertFits(String resource, long current, int maximum, boolean adding) {
        long projected = adding ? current + 1 : current;
        if (projected <= maximum) {
            return;
        }
        if (adding) {
            throw new DomainRuleException(
                    "This package allows " + maximum + " " + resource + ". The organisation already has " + current + ".");
        }
        throw new DomainRuleException(
                "This package allows " + maximum + " " + resource
                        + ". The organisation already has " + current
                        + ". Reduce that usage before changing package.");
    }
}
