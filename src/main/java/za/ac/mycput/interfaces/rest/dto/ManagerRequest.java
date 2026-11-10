package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record ManagerRequest(
        @NotBlank String managerId,
        @NotBlank String name,
        String contactNumber,
        String employeeId
) {
}
