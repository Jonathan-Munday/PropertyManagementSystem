package za.ac.mycput.interfaces.rest.dto;

import java.time.LocalDate;

public record LeaseResponse(
        String leaseId,
        String propertyId,
        String tenantName,
        double monthlyRent,
        String tenantId,
        LocalDate startDate,
        LocalDate endDate
) {
}
