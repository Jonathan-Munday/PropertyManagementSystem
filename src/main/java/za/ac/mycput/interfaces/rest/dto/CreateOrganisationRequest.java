package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganisationRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 200) String displayName
) {
}
