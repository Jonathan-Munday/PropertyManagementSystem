package za.ac.mycput.infrastructure.security;

import java.util.UUID;

/**
 * The person on the verified access token. The id is the JWT subject.
 * Email and verification state come from that token, not from a request body.
 */
public record AuthenticatedUser(UUID userId, String email, boolean emailVerified) {
}
