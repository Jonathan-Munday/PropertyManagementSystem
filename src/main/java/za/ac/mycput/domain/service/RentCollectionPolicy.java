package za.ac.mycput.domain.service;

import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.valueobject.Money;

/**
 * Rules for rent collected against a lease.
 * This is not the rule set for subscription billing.
 */
public class RentCollectionPolicy {

    public void assertCanRecord(String leaseId, double amount, String paymentMethod) {
        if (leaseId == null || leaseId.isBlank()) {
            throw new DomainRuleException("A rent payment must belong to a lease");
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new DomainRuleException("A rent payment needs a method");
        }
        if (!Money.zar(amount).isPositive()) {
            throw new DomainRuleException("A rent payment amount must be positive");
        }
    }
}
