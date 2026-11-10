package za.ac.mycput.domain.service;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.access.OrganisationRole;
import za.ac.mycput.domain.access.Permission;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolePermissionPolicyTest {

    private final RolePermissionPolicy policy = new RolePermissionPolicy();

    @Test
    void ownerCanDoEverythingAndIsTheOnlyRoleThatCanDeleteOrManageBilling() {
        for (Permission permission : Permission.values()) {
            assertTrue(policy.permits(OrganisationRole.OWNER, permission), permission.name());
        }
        assertTrue(policy.permits(OrganisationRole.ADMIN, Permission.VIEW_BILLING));
        assertFalse(policy.permits(OrganisationRole.ADMIN, Permission.MANAGE_BILLING));
        assertFalse(policy.permits(OrganisationRole.ADMIN, Permission.DELETE_ORGANISATION));
        for (OrganisationRole role : EnumSet.of(OrganisationRole.PROPERTY_MANAGER, OrganisationRole.ACCOUNTANT, OrganisationRole.VIEWER)) {
            assertFalse(policy.permits(role, Permission.MANAGE_BILLING), role.name());
            assertFalse(policy.permits(role, Permission.DELETE_ORGANISATION), role.name());
            assertFalse(policy.permits(role, Permission.MANAGE_USERS), role.name());
        }
    }

    @Test
    void portfolioWritesFollowTheRoleMatrix() {
        Set<OrganisationRole> writers = EnumSet.of(OrganisationRole.OWNER, OrganisationRole.ADMIN, OrganisationRole.PROPERTY_MANAGER);
        for (OrganisationRole role : OrganisationRole.values()) {
            boolean allowed = writers.contains(role);
            assertTrue(policy.permits(role, Permission.VIEW_PROPERTIES), role.name());
            assertTrue(policy.permits(role, Permission.VIEW_PAYMENTS), role.name());
            assertEquals(allowed, policy.permits(role, Permission.MANAGE_PROPERTIES), role.name());
            assertEquals(allowed, policy.permits(role, Permission.MANAGE_TENANTS), role.name());
            assertEquals(allowed, policy.permits(role, Permission.MANAGE_LEASES), role.name());
        }
        assertTrue(policy.permits(OrganisationRole.ACCOUNTANT, Permission.MANAGE_PAYMENTS));
        assertFalse(policy.permits(OrganisationRole.VIEWER, Permission.MANAGE_PAYMENTS));
        assertFalse(policy.permits(OrganisationRole.ACCOUNTANT, Permission.MANAGE_PROPERTIES));
        assertFalse(policy.permits(null, Permission.VIEW_PROPERTIES));
    }

    @Test
    void onlyAnOwnerCanGrantOwnership() {
        assertTrue(policy.canGrant(OrganisationRole.OWNER, OrganisationRole.OWNER));
        assertTrue(policy.canGrant(OrganisationRole.OWNER, OrganisationRole.ADMIN));
        assertTrue(policy.canGrant(OrganisationRole.ADMIN, OrganisationRole.PROPERTY_MANAGER));
        assertTrue(policy.canGrant(OrganisationRole.ADMIN, OrganisationRole.ACCOUNTANT));
        assertTrue(policy.canGrant(OrganisationRole.ADMIN, OrganisationRole.VIEWER));
        assertTrue(policy.canGrant(OrganisationRole.ADMIN, OrganisationRole.ADMIN));
        assertFalse(policy.canGrant(OrganisationRole.ADMIN, OrganisationRole.OWNER));
        assertFalse(policy.canGrant(OrganisationRole.PROPERTY_MANAGER, OrganisationRole.VIEWER));
        assertFalse(policy.canGrant(OrganisationRole.ACCOUNTANT, OrganisationRole.VIEWER));
        assertFalse(policy.canGrant(OrganisationRole.VIEWER, OrganisationRole.VIEWER));
    }

    private static void assertEquals(boolean expected, boolean actual, String role) {
        if (expected != actual) {
            throw new AssertionError(role);
        }
    }
}
