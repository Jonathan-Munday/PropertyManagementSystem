package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record TenantRequest(
        @NotBlank String tenantId,
        @NotBlank String firstName,
        String lastName,
        @NotBlank @Email String email,
        String phoneNumber
) {
}
