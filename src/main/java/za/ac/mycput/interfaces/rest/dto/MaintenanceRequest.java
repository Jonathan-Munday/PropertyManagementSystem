package za.ac.mycput.interfaces.rest.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record MaintenanceRequest(
        @NotBlank String requestId,
        @NotBlank String description,
        @NotBlank String status,
        @NotNull LocalDate reportedDate,
        @NotNull @PositiveOrZero Double cost
) {
}
