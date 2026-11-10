package za.ac.mycput.application.dto;

import java.util.UUID;

public record MemberResponse(
        UUID userId,
        String email,
        String displayName,
        String role,
        String status
) {
}
