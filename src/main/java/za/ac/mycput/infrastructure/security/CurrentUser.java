package za.ac.mycput.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import za.ac.mycput.domain.exception.OrganisationAccessException;
import za.ac.mycput.infrastructure.persistence.Identifiers;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthenticatedUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new OrganisationAccessException("The access token has no user id");
        }
        return from(jwt);
    }

    public static AuthenticatedUser from(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return new AuthenticatedUser(Identifiers.parse(jwt.getSubject()), email, verified(jwt));
    }

    private static boolean verified(Jwt jwt) {
        Object value = jwt.getClaim("email_verified");
        if (value instanceof Boolean flag) {
            return flag;
        }
        if (value instanceof String text) {
            return "true".equalsIgnoreCase(text);
        }
        return false;
    }
}
