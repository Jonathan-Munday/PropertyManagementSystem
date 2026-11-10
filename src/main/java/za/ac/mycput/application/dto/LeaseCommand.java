package za.ac.mycput.application.dto;

import java.time.LocalDate;

public record LeaseCommand(
        String leaseId,
        String propertyId,
        String tenantName,
        double monthlyRent,
        String tenantId,
        LocalDate startDate,
        LocalDate endDate
) {
    public LeaseCommand(String leaseId, String propertyId, String tenantName, double monthlyRent) {
        this(leaseId, propertyId, tenantName, monthlyRent, null, null, null);
    }
}
