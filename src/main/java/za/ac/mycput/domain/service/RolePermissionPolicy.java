package za.ac.mycput.domain.service;

import za.ac.mycput.domain.access.OrganisationRole;
import za.ac.mycput.domain.access.Permission;

import java.util.EnumSet;
import java.util.Map;

/**
 * The only map from an organisation role to what that role may do.
 * Callers ask this policy. They do not compare role names themselves.
 *
 * <p>Manage billing is the software subscription. An admin may view it and may not change it.
 * Recording rent is {@link Permission#MANAGE_PAYMENTS}, which is separate from that subscription.
 */
public final class RolePermissionPolicy {

    private static final EnumSet<Permission> ALL = EnumSet.allOf(Permission.class);

    private static final Map<OrganisationRole, EnumSet<Permission>> GRANTS = Map.of(
            OrganisationRole.OWNER, ALL,
            OrganisationRole.ADMIN, EnumSet.of(
                    Permission.VIEW_PROPERTIES,
                    Permission.MANAGE_PROPERTIES,
                    Permission.MANAGE_TENANTS,
                    Permission.MANAGE_LEASES,
                    Permission.VIEW_PAYMENTS,
                    Permission.MANAGE_PAYMENTS,
                    Permission.VIEW_OPERATIONS,
                    Permission.MANAGE_OPERATIONS,
                    Permission.VIEW_BILLING,
                    Permission.VIEW_MEMBERS,
                    Permission.MANAGE_USERS),
            OrganisationRole.PROPERTY_MANAGER, EnumSet.of(
                    Permission.VIEW_PROPERTIES,
                    Permission.MANAGE_PROPERTIES,
                    Permission.MANAGE_TENANTS,
                    Permission.MANAGE_LEASES,
                    Permission.VIEW_PAYMENTS,
                    Permission.MANAGE_PAYMENTS,
                    Permission.VIEW_OPERATIONS,
                    Permission.MANAGE_OPERATIONS,
                    Permission.VIEW_MEMBERS),
            OrganisationRole.ACCOUNTANT, EnumSet.of(
                    Permission.VIEW_PROPERTIES,
                    Permission.VIEW_PAYMENTS,
                    Permission.MANAGE_PAYMENTS,
                    Permission.VIEW_OPERATIONS,
                    Permission.VIEW_MEMBERS),
            OrganisationRole.VIEWER, EnumSet.of(
                    Permission.VIEW_PROPERTIES,
                    Permission.VIEW_PAYMENTS,
                    Permission.VIEW_OPERATIONS,
                    Permission.VIEW_MEMBERS)
    );

    public boolean permits(OrganisationRole role, Permission permission) {
        if (role == null || permission == null) {
            return false;
        }
        EnumSet<Permission> granted = GRANTS.get(role);
        return granted != null && granted.contains(permission);
    }

    /**
     * An owner may grant any role. An admin may grant every role except owner.
     * Nobody may grant a role they are not allowed to manage users for.
     */
    public boolean canGrant(OrganisationRole actor, OrganisationRole granted) {
        if (!permits(actor, Permission.MANAGE_USERS) || granted == null) {
            return false;
        }
        if (granted == OrganisationRole.OWNER) {
            return actor == OrganisationRole.OWNER;
        }
        return true;
    }
}
