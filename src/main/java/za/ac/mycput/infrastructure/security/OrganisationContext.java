package za.ac.mycput.infrastructure.security;

import za.ac.mycput.domain.exception.OrganisationAccessException;

public final class OrganisationContext {

    private static final ThreadLocal<OrganisationScope> CURRENT = new ThreadLocal<>();

    private OrganisationContext() {
    }

    public static void set(OrganisationScope scope) {
        CURRENT.set(scope);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static OrganisationScope require() {
        OrganisationScope scope = CURRENT.get();
        if (scope == null) {
            throw new OrganisationAccessException("No organisation is selected for this request");
        }
        return scope;
    }
}
