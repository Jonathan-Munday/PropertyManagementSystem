package za.ac.mycput.domain.service;

import za.ac.mycput.domain.exception.DomainRuleException;

import java.time.LocalDate;

public class LeaseTermPolicy {

    public void assertComplete(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new DomainRuleException("A lease needs a start date");
        }
        if (endDate == null) {
            throw new DomainRuleException("A lease needs an end date");
        }
        if (endDate.isBefore(startDate)) {
            throw new DomainRuleException("The end date cannot be before the start date");
        }
    }
}
