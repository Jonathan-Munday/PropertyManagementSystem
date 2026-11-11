package za.ac.mycput.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import za.ac.mycput.application.service.AccountApplicationService;
import za.ac.mycput.domain.access.OrganisationRole;
import za.ac.mycput.domain.exception.OrganisationAccessException;
import za.ac.mycput.domain.service.RolePermissionPolicy;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Inspects the status and JSON body the lease route actually returns.
 * The database is not involved. Membership rows are represented by the access service.
 */
class OrganisationAccessFilterTest {

    private static final UUID USER = UUID.fromString("11111111-1111-4111-8111-111111111101");
    private static final UUID ORG = UUID.fromString("22222222-2222-4222-8222-222222222221");

    private final OrganisationAccessService access = mock(OrganisationAccessService.class);
    private final AccountApplicationService accounts = mock(AccountApplicationService.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final OrganisationAccessFilter filter = new OrganisationAccessFilter(
            access, accounts, new ApiAuthorisationCatalog(), new RolePermissionPolicy(), false, "", "");

    @BeforeEach
    void authenticated() {
        SecurityContextHolder.getContext().setAuthentication(authentication(token(true)));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void aViewerCannotCreateALeaseAndTheBodyNamesTheRole() throws Exception {
        when(access.resolve(eq(USER), eq(ORG.toString())))
                .thenReturn(new OrganisationScope(USER, ORG, OrganisationRole.VIEWER));

        MockHttpServletResponse response = postLease(ORG.toString());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"error\":\"FORBIDDEN\""));
        assertTrue(response.getContentAsString().contains("viewer role cannot create or change leases"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void anAccountantCannotCreateALease() throws Exception {
        when(access.resolve(eq(USER), eq(ORG.toString())))
                .thenReturn(new OrganisationScope(USER, ORG, OrganisationRole.ACCOUNTANT));

        MockHttpServletResponse response = postLease(ORG.toString());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("accountant role cannot create or change leases"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void anOwnerAdminOrPropertyManagerIsAllowedThroughToTheLeaseEndpoint() throws Exception {
        for (OrganisationRole role : new OrganisationRole[]{
                OrganisationRole.OWNER, OrganisationRole.ADMIN, OrganisationRole.PROPERTY_MANAGER}) {
            when(access.resolve(eq(USER), eq(ORG.toString())))
                    .thenReturn(new OrganisationScope(USER, ORG, role));
            MockHttpServletResponse response = postLease(ORG.toString());
            assertEquals(200, response.getStatus(), role.name());
        }
        verify(chain, org.mockito.Mockito.times(3)).doFilter(any(), any());
    }

    @Test
    void aMembershipMissIsOrganisationAccessRatherThanALeasePermissionError() throws Exception {
        when(access.resolve(eq(USER), eq(ORG.toString())))
                .thenThrow(new OrganisationAccessException("You are not a member of that organisation"));

        MockHttpServletResponse response = postLease(ORG.toString());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"error\":\"ORGANISATION_ACCESS\""));
        assertTrue(response.getContentAsString().contains("You are not a member of that organisation"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void anExplicitlyUnverifiedEmailIsRejectedBeforeTheLeasePermissionCheck() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication(token(false)));

        MockHttpServletResponse response = postLease(ORG.toString());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"error\":\"EMAIL_NOT_VERIFIED\""));
        verify(access, never()).resolve(any(), any());
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void aPathOutsideTheLeaseResourceIsADifferentForbiddenResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/lease");
        request.addHeader("X-Organisation-Id", ORG.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("This action is not available"));
        verify(access, never()).resolve(any(), any());
    }

    private MockHttpServletResponse postLease(String organisationId) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/lease");
        request.addHeader("X-Organisation-Id", organisationId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    private static JwtAuthenticationToken authentication(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, AuthorityUtils.NO_AUTHORITIES);
    }

    private static Jwt token(boolean verified) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(USER.toString())
                .claim("email", "ada@example.com")
                .claim("email_verified", verified)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
