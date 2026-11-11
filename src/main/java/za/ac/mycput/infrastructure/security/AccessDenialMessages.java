package za.ac.mycput.infrastructure.security;

import za.ac.mycput.domain.access.OrganisationRole;
import za.ac.mycput.domain.access.Permission;

/**
 * Explains a refused organisation action using the caller's own membership.
 * It does not grant the action.
 */
public final class AccessDenialMessages {

    private AccessDenialMessages() {
    }

    public static String forPermission(Permission permission, OrganisationRole role) {
        String label = label(role);
        if (permission == Permission.MANAGE_LEASES) {
            return "Your " + label + " role cannot create or change leases. An owner, admin, or property manager can.";
        }
        return "Your " + label + " role does not have permission to do that.";
    }

    private static String label(OrganisationRole role) {
        if (role == null) {
            return "current";
        }
        return switch (role) {
            case OWNER -> "owner";
            case ADMIN -> "admin";
            case PROPERTY_MANAGER -> "property manager";
            case ACCOUNTANT -> "accountant";
            case VIEWER -> "viewer";
        };
    }
}
