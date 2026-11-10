package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record AgentRequest(
        @NotBlank String agentId,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String role
) {
}
