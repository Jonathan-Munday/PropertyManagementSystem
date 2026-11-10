package za.ac.mycput.application.dto;

import java.util.List;
import java.util.UUID;

public record AccountResponse(
        UUID userId,
        String email,
        String displayName,
        boolean emailVerified,
        boolean profileComplete,
        List<OrganisationMembershipResponse> organisations
) {
}
