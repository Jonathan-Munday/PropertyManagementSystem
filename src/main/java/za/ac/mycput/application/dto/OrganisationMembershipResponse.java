package za.ac.mycput.application.dto;

import java.util.UUID;

public record OrganisationMembershipResponse(
        UUID organisationId,
        String organisationName,
        String role,
        String status
) {
}
