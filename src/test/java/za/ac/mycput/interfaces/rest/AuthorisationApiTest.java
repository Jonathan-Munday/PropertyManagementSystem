package za.ac.mycput.interfaces.rest;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import za.ac.mycput.infrastructure.security.JwksServer;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authentication, role, and cross-organisation checks against PostgreSQL.
 * Skipped when Docker is not available. Cryptographic cases that do not need
 * the database live in {@code SupabaseJwtDecoderTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthorisationApiTest {

    static final String ISSUER = "https://test-project.supabase.co/auth/v1";
    static final String AUDIENCE = "authenticated";
    static final UUID ORG_A = UUID.fromString("22222222-2222-4222-8222-222222222221");
    static final UUID ORG_B = UUID.fromString("22222222-2222-4222-8222-222222222222");
    static final UUID OWNER = UUID.fromString("11111111-1111-4111-8111-111111111101");
    static final UUID ADMIN = UUID.fromString("11111111-1111-4111-8111-111111111102");
    static final UUID MANAGER = UUID.fromString("11111111-1111-4111-8111-111111111103");
    static final UUID ACCOUNTANT = UUID.fromString("11111111-1111-4111-8111-111111111104");
    static final UUID VIEWER = UUID.fromString("11111111-1111-4111-8111-111111111105");
    static final UUID OWNER_B = UUID.fromString("11111111-1111-4111-8111-111111111106");

    static final RSAKey SIGNING_KEY;
    static final JwksServer JWKS;

    static {
        try {
            SIGNING_KEY = new RSAKeyGenerator(2048).keyID("api-test").generate();
            JWKS = JwksServer.start();
            JWKS.publish(SIGNING_KEY);
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("portico.security.jwt.jwk-set-uri", JWKS::uri);
        registry.add("portico.security.jwt.issuer", () -> ISSUER);
        registry.add("portico.security.jwt.audience", () -> AUDIENCE);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void members() {
        PortfolioFixtures.membership(jdbc, OWNER, ORG_A, "Harbour Lettings", "OWNER");
        PortfolioFixtures.membership(jdbc, ADMIN, ORG_A, "Harbour Lettings", "ADMIN");
        PortfolioFixtures.membership(jdbc, MANAGER, ORG_A, "Harbour Lettings", "PROPERTY_MANAGER");
        PortfolioFixtures.membership(jdbc, ACCOUNTANT, ORG_A, "Harbour Lettings", "ACCOUNTANT");
        PortfolioFixtures.membership(jdbc, VIEWER, ORG_A, "Harbour Lettings", "VIEWER");
        PortfolioFixtures.membership(jdbc, OWNER_B, ORG_B, "Other Desk", "OWNER");
        PortfolioFixtures.agencyDemo(jdbc, ORG_A);
        PortfolioFixtures.agencyDemo(jdbc, ORG_B);
    }

    @Test
    void missingAndInvalidTokensAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/property")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/property").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token(UUID.randomUUID(), "ada@example.com", Instant.now().minusSeconds(300), ISSUER, AUDIENCE)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token(UUID.randomUUID(), "ada@example.com", Instant.now().plusSeconds(300), ISSUER, "anon")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token(UUID.randomUUID(), "ada@example.com", Instant.now().plusSeconds(300), "https://evil.example/auth/v1", AUDIENCE)))
                .andExpect(status().isUnauthorized());

        RSAKey other = new RSAKeyGenerator(2048).keyID(SIGNING_KEY.getKeyID()).generate();
        SignedJWT forged = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(other.getKeyID()).type(JOSEObjectType.JWT).build(),
                claims(UUID.randomUUID(), "ada@example.com", Instant.now().plusSeconds(300), ISSUER, AUDIENCE));
        forged.sign(new RSASSASigner(other));
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + forged.serialize()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aSignedTokenIdentifiesTheSubjectAndCannotUseAnotherOrganisation() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token(userId, "new@example.com", Instant.now().plusSeconds(300), ISSUER, AUDIENCE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.organisations").isEmpty());

        mockMvc.perform(get("/api/property").header("Authorization", "Bearer " + token(userId, "new@example.com", Instant.now().plusSeconds(300), ISSUER, AUDIENCE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ORGANISATION_ACCESS"));
    }

    @Test
    void anUnverifiedEmailCannotCallTheApi() throws Exception {
        JWTClaimsSet unverified = new JWTClaimsSet.Builder()
                .subject(OWNER.toString())
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .issueTime(Date.from(Instant.now().minusSeconds(30)))
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("email", "owner@example.com")
                .claim("email_verified", false)
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(SIGNING_KEY.getKeyID()).type(JOSEObjectType.JWT).build(), unverified);
        jwt.sign(new RSASSASigner(SIGNING_KEY));
        mockMvc.perform(get("/api/property").header("Authorization", "Bearer " + jwt.serialize()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("EMAIL_NOT_VERIFIED"));
    }

    @Test
    void rolesAreEnforcedFromTheMembershipRatherThanTheRequest() throws Exception {
        mockMvc.perform(get("/api/property").with(as(VIEWER, "viewer@example.com"))).andExpect(status().isOk());
        mockMvc.perform(post("/api/property").with(as(VIEWER, "viewer@example.com"))
                        .contentType("application/json")
                        .content(property("33333333-3333-4333-8333-333333333301", "Viewer Street", 1000)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        mockMvc.perform(get("/api/payment").with(as(ACCOUNTANT, "accounts@example.com"))).andExpect(status().isOk());
        mockMvc.perform(get("/api/billing/subscription").with(as(ACCOUNTANT, "accounts@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        mockMvc.perform(post("/api/property").with(as(ACCOUNTANT, "accounts@example.com"))
                        .contentType("application/json")
                        .content(property("33333333-3333-4333-8333-333333333302", "Ledger Street", 1000)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/billing/subscription").with(as(ADMIN, "admin@example.com")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/billing/subscription").with(as(ADMIN, "admin@example.com"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/organisations/" + ORG_A).with(as(ADMIN, "admin@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        mockMvc.perform(post("/api/billing/subscription").with(as(OWNER, "owner@example.com"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        mockMvc.perform(post("/api/property").with(as(MANAGER, "manager@example.com"))
                        .contentType("application/json")
                        .content(property("33333333-3333-4333-8333-333333333303", "Manager Street", 2500)))
                .andExpect(status().isCreated());
    }

    @Test
    void aCallerCannotReadOrJoinAnotherOrganisationByChangingIds() throws Exception {
        String secretId = "33333333-3333-4333-8333-333333333310";
        mockMvc.perform(post("/api/property").with(as(OWNER, "owner@example.com"))
                        .contentType("application/json")
                        .content("""
                                {"propertyId":"%s","address":"Secret Wharf","description":"Flat","rentalAmount":9000,"available":true,"organisationId":"%s","userId":"%s"}
                                """.formatted(secretId, ORG_B, OWNER_B)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/property/" + secretId).with(as(OWNER, "owner@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Secret Wharf"));
        mockMvc.perform(get("/api/property/" + secretId).with(as(OWNER_B, "other@example.com")))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/property/" + secretId)
                        .with(as(OWNER_B, "other@example.com"))
                        .header("X-Organisation-Id", ORG_A)
                        .param("organisationId", ORG_A.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ORGANISATION_ACCESS"))
                .andExpect(content().string(not(containsString("Secret Wharf"))));
        mockMvc.perform(get("/api/property").with(as(OWNER_B, "other@example.com")).param("organisationId", ORG_A.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Secret Wharf"))));
        mockMvc.perform(get("/api/v1/organisations/" + ORG_A + "/members").with(as(OWNER_B, "other@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString("owner@example.com"))));

        Integer inOtherOrg = jdbc.queryForObject(
                "select count(*) from properties where property_id = ? and organisation_id = ?",
                Integer.class, UUID.fromString(secretId), ORG_B);
        org.junit.jupiter.api.Assertions.assertEquals(0, inOtherOrg);
    }

    @Test
    void severalMembershipsRequireAnOrganisationTheUserActuallyBelongsTo() throws Exception {
        UUID person = UUID.randomUUID();
        PortfolioFixtures.membership(jdbc, person, ORG_A, "Harbour Lettings", "OWNER");
        PortfolioFixtures.membership(jdbc, person, ORG_B, "Other Desk", "VIEWER");
        mockMvc.perform(get("/api/property").with(as(person, "both@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ORGANISATION_ACCESS"));
        mockMvc.perform(get("/api/property").with(as(person, "both@example.com")).header("X-Organisation-Id", ORG_A))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/property").with(as(person, "both@example.com")).header("X-Organisation-Id", UUID.randomUUID()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/property").with(as(person, "both@example.com")).header("X-Organisation-Id", ORG_B)
                        .contentType("application/json")
                        .content(property("33333333-3333-4333-8333-333333333320", "Should Fail", 1000)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void creatingAnOrganisationMakesTheSignedInUserItsOwner() throws Exception {
        UUID intruder = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/organisations").with(as(intruder, "new-owner@example.com"))
                        .contentType("application/json")
                        .content("""
                                {"name":"New Desk","displayName":"Amina Jacobs","role":"ADMIN","userId":"%s","organisationId":"%s"}
                                """.formatted(OWNER, ORG_A)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.organisationName").value("New Desk"))
                .andExpect(jsonPath("$.organisationId").value(not(ORG_A.toString())));

        String role = jdbc.queryForObject(
                "select role from organisation_memberships where user_id = ? and organisation_id <> ?",
                String.class, intruder, ORG_A);
        org.junit.jupiter.api.Assertions.assertEquals("OWNER", role);
        Integer stolen = jdbc.queryForObject(
                "select count(*) from organisation_memberships where user_id = ? and organisation_id = ? and role = 'ADMIN'",
                Integer.class, intruder, ORG_A);
        org.junit.jupiter.api.Assertions.assertEquals(0, stolen);

        mockMvc.perform(put("/api/v1/me").with(as(intruder, "new-owner@example.com"))
                        .contentType("application/json")
                        .content("{\"displayName\":\"Amina Jacobs\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileComplete").value(true));
    }

    @Test
    void invitationsGrantOnlyTheStoredRoleToTheInvitedEmail() throws Exception {
        mockMvc.perform(post("/api/v1/organisations/" + ORG_A + "/invitations").with(as(VIEWER, "viewer@example.com"))
                        .contentType("application/json")
                        .content("{\"email\":\"guest@example.com\",\"role\":\"OWNER\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/organisations/" + ORG_A + "/invitations").with(as(ADMIN, "admin@example.com"))
                        .contentType("application/json")
                        .content("{\"email\":\"guest@example.com\",\"role\":\"OWNER\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        String token = mockMvc.perform(post("/api/v1/organisations/" + ORG_A + "/invitations").with(as(OWNER, "owner@example.com"))
                        .contentType("application/json")
                        .content("{\"email\":\"guest@example.com\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("VIEWER"))
                .andReturn().getResponse().getContentAsString();
        String invitationToken = token.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        UUID guest = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/invitations/accept").with(as(guest, "someone-else@example.com"))
                        .contentType("application/json")
                        .header("X-Organisation-Id", ORG_B)
                        .content("{\"token\":\"" + invitationToken + "\",\"role\":\"OWNER\",\"userId\":\"" + OWNER + "\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/invitations/accept").with(as(guest, "guest@example.com"))
                        .contentType("application/json")
                        .header("X-Organisation-Id", ORG_B)
                        .content("{\"token\":\"" + invitationToken + "\",\"role\":\"OWNER\",\"userId\":\"" + OWNER + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organisationId").value(ORG_A.toString()))
                .andExpect(jsonPath("$.role").value("VIEWER"));

        String storedRole = jdbc.queryForObject(
                "select role from organisation_memberships where user_id = ? and organisation_id = ?",
                String.class, guest, ORG_A);
        org.junit.jupiter.api.Assertions.assertEquals("VIEWER", storedRole);
        Integer joinedOther = jdbc.queryForObject(
                "select count(*) from organisation_memberships where user_id = ? and organisation_id = ?",
                Integer.class, guest, ORG_B);
        org.junit.jupiter.api.Assertions.assertEquals(0, joinedOther);

        mockMvc.perform(post("/api/property").with(as(guest, "guest@example.com"))
                        .contentType("application/json")
                        .content(property("33333333-3333-4333-8333-333333333330", "Guest Street", 1000)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/property").with(as(guest, "guest@example.com"))).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/invitations/accept").with(as(guest, "guest@example.com"))
                        .contentType("application/json")
                        .content("{\"token\":\"" + invitationToken + "\",\"role\":\"OWNER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void theOwnerCanDeleteAnEmptyOrganisationAndAMemberCanSeeTheRoster() throws Exception {
        UUID founder = UUID.randomUUID();
        String created = mockMvc.perform(post("/api/v1/organisations").with(as(founder, "founder@example.com"))
                        .contentType("application/json")
                        .content("{\"name\":\"Temporary Desk\",\"displayName\":\"Harbour Owner\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String organisationId = created.replaceAll(".*\"organisationId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/organisations/" + organisationId + "/members").with(as(founder, "founder@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[0].userId").value(founder.toString()));
        mockMvc.perform(delete("/api/v1/organisations/" + organisationId).with(as(founder, "founder@example.com")))
                .andExpect(status().isNoContent());
    }

    @Test
    void creatingALeaseRequiresManageLeasesAndTheCallersOrganisation() throws Exception {
        String propertyId = "33333333-3333-4333-8333-333333333340";
        String adminPropertyId = "33333333-3333-4333-8333-333333333341";
        String managerPropertyId = "33333333-3333-4333-8333-333333333342";
        String tenantId = "33333333-3333-4333-8333-333333333343";
        mockMvc.perform(post("/api/property").with(as(OWNER, "owner@example.com")).contentType("application/json")
                        .content(property(propertyId, "Lease Wharf", 8000)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/property").with(as(OWNER, "owner@example.com")).contentType("application/json")
                        .content(property(adminPropertyId, "Admin Wharf", 8100)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/property").with(as(OWNER, "owner@example.com")).contentType("application/json")
                        .content(property(managerPropertyId, "Manager Wharf", 8200)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tenant").with(as(OWNER, "owner@example.com")).contentType("application/json")
                        .content("""
                                {"tenantId":"%s","firstName":"Ada","lastName":"Lovelace","email":"ada.lease-role@example.com","phoneNumber":"0210000099"}
                                """.formatted(tenantId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/lease").with(as(VIEWER, "viewer@example.com")).contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value(containsString("viewer role cannot create or change leases")));
        mockMvc.perform(post("/api/lease").with(as(ACCOUNTANT, "accounts@example.com")).contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value(containsString("accountant role cannot create or change leases")));
        mockMvc.perform(post("/api/lease").with(as(OWNER, "owner@example.com"))
                        .header("X-Organisation-Id", ORG_B)
                        .contentType("application/json")
                        .content(lease("33333333-3333-4333-8333-333333333344", propertyId, tenantId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ORGANISATION_ACCESS"));

        mockMvc.perform(post("/api/lease").with(as(OWNER, "owner@example.com")).contentType("application/json")
                        .content(lease("33333333-3333-4333-8333-333333333344", propertyId, tenantId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/lease").with(as(ADMIN, "admin@example.com")).contentType("application/json")
                        .content(lease("33333333-3333-4333-8333-333333333345", adminPropertyId, tenantId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/lease").with(as(MANAGER, "manager@example.com")).contentType("application/json")
                        .content(lease("33333333-3333-4333-8333-333333333346", managerPropertyId, tenantId)))
                .andExpect(status().isCreated());

        UUID suspended = UUID.randomUUID();
        PortfolioFixtures.membership(jdbc, suspended, ORG_A, "Harbour Lettings", "OWNER");
        jdbc.update("update organisation_memberships set status = 'SUSPENDED' where user_id = ?", suspended);
        mockMvc.perform(post("/api/lease").with(as(suspended, "suspended@example.com")).contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ORGANISATION_ACCESS"));
    }

    @Test
    void demoCheckoutIsNotAPaidSubscriptionAndThePackageCapIsEnforced() throws Exception {
        UUID founder = UUID.randomUUID();
        String created = mockMvc.perform(post("/api/v1/organisations").with(as(founder, "billing-owner@example.com"))
                        .contentType("application/json")
                        .content("{\"name\":\"Billing Desk\",\"displayName\":\"Billing Owner\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String organisationId = created.replaceAll(".*\"organisationId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/billing/plans").with(as(VIEWER, "viewer@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        mockMvc.perform(get("/api/billing/plans").with(as(founder, "billing-owner@example.com"))
                        .header("X-Organisation-Id", organisationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("STARTER"))
                .andExpect(jsonPath("$[0].amount").value(197.0))
                .andExpect(jsonPath("$[3].code").value("AGENCY"))
                .andExpect(jsonPath("$[3].amount").value(1979.0));

        mockMvc.perform(post("/api/billing/subscription").with(as(ADMIN, "admin@example.com"))
                        .contentType("application/json")
                        .content("{\"planCode\":\"GROWTH\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/billing/subscription").with(as(founder, "billing-owner@example.com"))
                        .header("X-Organisation-Id", organisationId)
                        .contentType("application/json")
                        .content("{\"planCode\":\"STARTER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("demo"))
                .andExpect(jsonPath("$.status").value("DEMO"))
                .andExpect(jsonPath("$.billingMode").value("DEMO"))
                .andExpect(jsonPath("$.demoCheckout").value(true))
                .andExpect(jsonPath("$.liveChargesEnabled").value(false))
                .andExpect(jsonPath("$.plan.code").value("STARTER"))
                .andExpect(jsonPath("$.usage.maxProperties").value(5))
                .andExpect(jsonPath("$.history[0].status").value("DEMO"))
                .andExpect(jsonPath("$.history[0].detail").value(containsString("No payment was collected")));

        for (int index = 0; index < 5; index++) {
            mockMvc.perform(post("/api/property").with(as(founder, "billing-owner@example.com"))
                            .header("X-Organisation-Id", organisationId)
                            .contentType("application/json")
                            .content(property(UUID.randomUUID().toString(), "Package Street " + index, 1000)))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/property").with(as(founder, "billing-owner@example.com"))
                        .header("X-Organisation-Id", organisationId)
                        .contentType("application/json")
                        .content(property(UUID.randomUUID().toString(), "Over the cap", 1000)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DOMAIN_RULE"))
                .andExpect(jsonPath("$.message").value(containsString("properties")));

        String invited = mockMvc.perform(post("/api/v1/organisations/" + organisationId + "/invitations")
                        .with(as(founder, "billing-owner@example.com"))
                        .header("X-Organisation-Id", organisationId)
                        .contentType("application/json")
                        .content("{\"email\":\"billing-guest@example.com\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String invitationToken = invited.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        UUID guest = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/invitations/accept").with(as(guest, "billing-guest@example.com"))
                        .contentType("application/json")
                        .content("{\"token\":\"" + invitationToken + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/organisations/" + organisationId + "/invitations")
                        .with(as(founder, "billing-owner@example.com"))
                        .header("X-Organisation-Id", organisationId)
                        .contentType("application/json")
                        .content("{\"email\":\"billing-extra@example.com\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DOMAIN_RULE"))
                .andExpect(jsonPath("$.message").value(containsString("members")));

        mockMvc.perform(post("/api/billing/subscription/cancel").with(as(founder, "billing-owner@example.com"))
                        .header("X-Organisation-Id", organisationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.billingMode").value("CANCELED"));
        mockMvc.perform(post("/api/property").with(as(founder, "billing-owner@example.com"))
                        .header("X-Organisation-Id", organisationId)
                        .contentType("application/json")
                        .content(property(UUID.randomUUID().toString(), "After cancel", 1000)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("canceled")));
    }

    private JwtRequestPostProcessor as(UUID userId, String email) {
        return jwt().jwt(token -> token.subject(userId.toString())
                .claim("email", email)
                .claim("email_verified", true));
    }

    private String token(UUID subject, String email, Instant expiresAt, String issuer, String audience) throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(SIGNING_KEY.getKeyID()).type(JOSEObjectType.JWT).build(),
                claims(subject, email, expiresAt, issuer, audience));
        jwt.sign(new RSASSASigner(SIGNING_KEY));
        return jwt.serialize();
    }

    private JWTClaimsSet claims(UUID subject, String email, Instant expiresAt, String issuer, String audience) {
        return new JWTClaimsSet.Builder()
                .subject(subject.toString())
                .issuer(issuer)
                .audience(audience)
                .issueTime(Date.from(expiresAt.minusSeconds(120)))
                .expirationTime(Date.from(expiresAt))
                .claim("email", email)
                .claim("email_verified", true)
                .build();
    }

    private String property(String id, String address, int rent) {
        return """
                {"propertyId":"%s","address":"%s","description":"Flat","rentalAmount":%d,"available":true}
                """.formatted(id, address, rent);
    }

    private String lease(String leaseId, String propertyId, String tenantId) {
        return """
                {"leaseId":"%s","propertyId":"%s","tenantId":"%s","tenantName":"Ada Lovelace","monthlyRent":8000,"startDate":"2026-01-01","endDate":"2027-01-01"}
                """.formatted(leaseId, propertyId, tenantId);
    }
}
