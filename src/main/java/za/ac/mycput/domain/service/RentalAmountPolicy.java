package za.ac.mycput.domain.service;

import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.valueobject.Money;

public class RentalAmountPolicy {

    public void assertNonNegative(double rentalAmount) {
        if (Money.zar(rentalAmount).isNegative()) {
            throw new DomainRuleException("Rental amount cannot be negative");
        }
    }
}
