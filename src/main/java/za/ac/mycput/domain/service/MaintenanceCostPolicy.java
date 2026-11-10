package za.ac.mycput.domain.service;

import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.model.MaintenanceStatus;
import za.ac.mycput.domain.valueobject.Money;

public class MaintenanceCostPolicy {

    public void assertValid(String description, String status, double cost) {
        if (description == null || description.isBlank()) {
            throw new DomainRuleException("A maintenance request needs a description");
        }
        MaintenanceStatus.parse(status);
        if (Money.zar(cost).isNegative()) {
            throw new DomainRuleException("Maintenance cost cannot be negative");
        }
    }
}
