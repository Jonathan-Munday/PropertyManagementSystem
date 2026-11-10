package za.ac.mycput.infrastructure.security;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.access.Permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiAuthorisationCatalogTest {

    private final ApiAuthorisationCatalog catalog = new ApiAuthorisationCatalog();

    @Test
    void identityRoutesDoNotRequireAnOrganisation() {
        assertEquals(ApiAuthorisationCatalog.Kind.AUTHENTICATED, catalog.ruleFor("GET", "/api/v1/me").kind());
        assertEquals(ApiAuthorisationCatalog.Kind.AUTHENTICATED, catalog.ruleFor("PUT", "/api/v1/me").kind());
        assertEquals(ApiAuthorisationCatalog.Kind.AUTHENTICATED, catalog.ruleFor("POST", "/api/v1/organisations").kind());
        assertEquals(ApiAuthorisationCatalog.Kind.AUTHENTICATED, catalog.ruleFor("POST", "/api/v1/invitations/accept").kind());
        assertNull(catalog.ruleFor("GET", "/api/v1/me").permission());
    }

    @Test
    void portfolioRoutesUseOnePermissionForTheWholeCollection() {
        assertEquals(Permission.VIEW_PROPERTIES, catalog.ruleFor("GET", "/api/property").permission());
        assertEquals(Permission.MANAGE_PROPERTIES, catalog.ruleFor("POST", "/api/property").permission());
        assertEquals(Permission.MANAGE_PROPERTIES, catalog.ruleFor("DELETE", "/api/property/33333333-3333-4333-8333-333333333331").permission());
        assertEquals(Permission.VIEW_PAYMENTS, catalog.ruleFor("GET", "/api/payment").permission());
        assertEquals(Permission.MANAGE_PAYMENTS, catalog.ruleFor("POST", "/api/payment").permission());
        assertEquals(Permission.MANAGE_TENANTS, catalog.ruleFor("PUT", "/api/tenant/33333333-3333-4333-8333-333333333331").permission());
        assertEquals(Permission.MANAGE_LEASES, catalog.ruleFor("POST", "/api/lease").permission());
        assertEquals(Permission.VIEW_BILLING, catalog.ruleFor("GET", "/api/billing/subscription").permission());
        assertEquals(Permission.MANAGE_BILLING, catalog.ruleFor("POST", "/api/billing/subscription").permission());
        assertEquals(Permission.MANAGE_USERS, catalog.ruleFor("POST", "/api/v1/organisations/22222222-2222-4222-8222-222222222222/invitations").permission());
        assertEquals(Permission.DELETE_ORGANISATION, catalog.ruleFor("DELETE", "/api/v1/organisations/22222222-2222-4222-8222-222222222222").permission());
        assertEquals(Permission.VIEW_MEMBERS, catalog.ruleFor("GET", "/api/v1/organisations/22222222-2222-4222-8222-222222222222/members").permission());
    }

    @Test
    void unknownApiRoutesAreDenied() {
        assertEquals(ApiAuthorisationCatalog.Kind.DENY, catalog.ruleFor("GET", "/api/v1/organisations/not-a-uuid").kind());
        assertEquals(ApiAuthorisationCatalog.Kind.DENY, catalog.ruleFor("GET", "/api/admin/users").kind());
        assertEquals(ApiAuthorisationCatalog.Kind.DENY, catalog.ruleFor("POST", "/api/property-extra").kind());
    }
}
