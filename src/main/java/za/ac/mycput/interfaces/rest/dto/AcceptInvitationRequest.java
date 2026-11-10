package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(@NotBlank @Size(max = 80) String token) {
}
