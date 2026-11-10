package za.ac.mycput.interfaces.rest.dto;

import java.time.LocalDate;

public record MaintenanceResponse(
        String requestId,
        String description,
        String status,
        LocalDate reportedDate,
        double cost
) {
}
