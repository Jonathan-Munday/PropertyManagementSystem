package za.ac.mycput.application.dto;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID invitationId,
        String email,
        String role,
        String status,
        Instant expiresAt,
        String token
) {
}
