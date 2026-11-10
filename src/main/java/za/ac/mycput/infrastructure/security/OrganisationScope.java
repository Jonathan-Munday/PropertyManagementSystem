package za.ac.mycput.infrastructure.security;

import za.ac.mycput.domain.access.OrganisationRole;

import java.util.UUID;

/**
 * The caller and the organisation membership that authorised this request.
 * Both values are resolved on the server from the verified token.
 */
public record OrganisationScope(UUID userId, UUID organisationId, OrganisationRole role) {
}
