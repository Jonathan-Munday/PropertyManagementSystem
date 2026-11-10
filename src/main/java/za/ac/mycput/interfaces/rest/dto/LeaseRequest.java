package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record LeaseRequest(
        @NotBlank String leaseId,
        @NotBlank String propertyId,
        @NotBlank String tenantName,
        @NotNull @Positive Double monthlyRent,
        @NotBlank String tenantId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
