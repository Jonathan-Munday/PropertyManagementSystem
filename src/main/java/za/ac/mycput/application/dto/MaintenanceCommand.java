package za.ac.mycput.application.dto;

import java.time.LocalDate;

public record MaintenanceCommand(
        String requestId,
        String description,
        String status,
        LocalDate reportedDate,
        double cost
) {
}
