package za.ac.mycput.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Subscription caps, demo checkout, and invoice organisation checks.
 * Skipped when Docker is not available.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class SubscriptionAuditTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void twoPendingInvitationsCannotExceedTheMemberLimit() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "cap-owner@example.com", "Cap Desk");

        mockMvc.perform(post("/api/v1/organisations/" + organisationId + "/invitations")
                        .with(as(owner, "cap-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"email\":\"first-seat@example.com\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/organisations/" + organisationId + "/invitations")
                        .with(as(owner, "cap-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"email\":\"second-seat@example.com\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DOMAIN_RULE"))
                .andExpect(jsonPath("$.message").value(containsString("members")));

        assertEquals(1L, count("""
                select count(*) from organisation_invitations
                where organisation_id = ? and status = 'PENDING'
                """, organisationId));
    }

    @Test
    void acceptingAnInvitationCannotExceedTheMemberLimit() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "accept-owner@example.com", "Accept Desk");
        String invited = mockMvc.perform(post("/api/v1/organisations/" + organisationId + "/invitations")
                        .with(as(owner, "accept-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"email\":\"accepted-seat@example.com\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String token = invited.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        UUID guest = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/invitations/accept").with(as(guest, "accepted-seat@example.com"))
                        .contentType("application/json")
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk());

        String overflowToken = "overflow-" + UUID.randomUUID();
        jdbc.update("""
                insert into organisation_invitations
                    (id, organisation_id, email, role, status, token, expires_at, invited_by)
                values (?, ?, 'overflow@example.com', 'VIEWER', 'PENDING', ?, now() + interval '7 days', ?)
                """, UUID.randomUUID(), organisationId, overflowToken, owner);
        UUID overflow = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/invitations/accept").with(as(overflow, "overflow@example.com"))
                        .contentType("application/json")
                        .content("{\"token\":\"" + overflowToken + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("members")));
        assertEquals(2L, count("""
                select count(*) from organisation_memberships
                where organisation_id = ? and status = 'ACTIVE'
                """, organisationId));
    }

    @Test
    void concurrentInvitationsCannotExceedTheMemberLimit() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "race-owner@example.com", "Race Desk");
        int[] statuses = race(() -> mockMvc.perform(post("/api/v1/organisations/" + organisationId + "/invitations")
                        .with(as(owner, "race-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"email\":\"race-a@example.com\",\"role\":\"VIEWER\"}"))
                .andReturn().getResponse().getStatus(), () -> mockMvc.perform(post("/api/v1/organisations/" + organisationId + "/invitations")
                        .with(as(owner, "race-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"email\":\"race-b@example.com\",\"role\":\"VIEWER\"}"))
                .andReturn().getResponse().getStatus());
        assertEquals(1, countStatus(statuses, 201));
        assertEquals(1, countStatus(statuses, 400));
        assertEquals(1L, count("""
                select count(*) from organisation_invitations
                where organisation_id = ? and status = 'PENDING'
                """, organisationId));
    }

    @Test
    void concurrentPropertyCreationCannotExceedThePropertyLimit() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "property-owner@example.com", "Property Cap");
        for (int index = 0; index < 4; index++) {
            createProperty(owner, "property-owner@example.com", organisationId, "Filled " + index);
        }
        int[] statuses = race(
                () -> createPropertyStatus(owner, organisationId, "Race A"),
                () -> createPropertyStatus(owner, organisationId, "Race B"));
        assertEquals(1, countStatus(statuses, 201));
        assertEquals(1, countStatus(statuses, 400));
        assertEquals(5L, count("select count(*) from properties where organisation_id = ?", organisationId));
    }

    @Test
    void anOrganisationWithoutASubscriptionUsesStarterDemoLimits() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = UUID.randomUUID();
        jdbc.update("insert into organisations (id, name) values (?, ?)", organisationId, "Legacy Desk");
        jdbc.update("insert into user_profiles (id, email, display_name) values (?, ?, ?)",
                owner, "legacy-owner@example.com", "Legacy Owner");
        jdbc.update("""
                insert into organisation_memberships (id, organisation_id, user_id, role, status)
                values (?, ?, ?, 'OWNER', 'ACTIVE')
                """, UUID.randomUUID(), organisationId, owner);
        for (int index = 0; index < 6; index++) {
            jdbc.update("""
                    insert into properties (property_id, organisation_id, address, description, rental_amount, available)
                    values (?, ?, ?, 'Flat', 1000, true)
                    """, UUID.randomUUID(), organisationId, "Legacy Road " + index);
        }

        mockMvc.perform(get("/api/billing/subscription").with(as(owner, "legacy-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("demo"))
                .andExpect(jsonPath("$.status").value("DEMO"))
                .andExpect(jsonPath("$.plan.code").value("STARTER"))
                .andExpect(jsonPath("$.usage.properties").value(6))
                .andExpect(jsonPath("$.usage.maxProperties").value(5))
                .andExpect(jsonPath("$.usage.maxUsers").value(2))
                .andExpect(jsonPath("$.liveChargesEnabled").value(false));
        mockMvc.perform(get("/api/property").with(as(owner, "legacy-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
        mockMvc.perform(post("/api/property").with(as(owner, "legacy-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content(property(UUID.randomUUID(), "One more")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("properties")));
        assertEquals(0L, count("select count(*) from organisation_subscriptions where organisation_id = ?", organisationId));
        assertEquals(6L, count("select count(*) from properties where organisation_id = ?", organisationId));
    }

    @Test
    void aCanceledSubscriptionCanBeReplacedBySimulatedCheckout() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "replace-owner@example.com", "Replace Desk");
        UUID original = subscriptionId(organisationId);

        mockMvc.perform(post("/api/billing/subscription/cancel").with(as(owner, "replace-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
        mockMvc.perform(post("/api/billing/subscription").with(as(owner, "replace-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"planCode\":\"GROWTH\",\"outcome\":\"SUCCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEMO"))
                .andExpect(jsonPath("$.plan.code").value("GROWTH"))
                .andExpect(jsonPath("$.liveChargesEnabled").value(false));

        assertEquals("CANCELED", text("select status from organisation_subscriptions where id = ?", original));
        UUID replacement = subscriptionId(organisationId, "DEMO");
        assertNotEquals(original, replacement);
        assertEquals(1L, count("""
                select count(*) from organisation_subscriptions
                where organisation_id = ? and status = 'DEMO'
                """, organisationId));
        assertEquals(0L, count("""
                select count(*) from organisation_subscriptions
                where organisation_id = ? and status = 'ACTIVE'
                """, organisationId));
    }

    @Test
    void anAdminCannotChangeOrCancelASubscription() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "admin-owner@example.com", "Admin Desk");
        UUID admin = UUID.randomUUID();
        membership(admin, organisationId, "admin-billing@example.com", "ADMIN");

        mockMvc.perform(post("/api/billing/subscription").with(as(admin, "admin-billing@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"planCode\":\"GROWTH\",\"outcome\":\"SUCCESS\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/billing/subscription/cancel").with(as(admin, "admin-billing@example.com"))
                        .header("X-Organisation-Id", organisationId.toString()))
                .andExpect(status().isForbidden());
        assertEquals("DEMO", text("select status from organisation_subscriptions where organisation_id = ?", organisationId));
        assertEquals("STARTER", text("""
                select plan.code from organisation_subscriptions subscription
                join subscription_plans plan on plan.id = subscription.plan_id
                where subscription.organisation_id = ?
                """, organisationId));
    }

    @Test
    void aNonMemberCannotReadAnotherOrganisationsSubscription() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "private-owner@example.com", "Private Desk");
        UUID stranger = UUID.randomUUID();

        mockMvc.perform(get("/api/billing/subscription").with(as(stranger, "stranger@example.com"))
                        .header("X-Organisation-Id", organisationId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ORGANISATION_ACCESS"))
                .andExpect(jsonPath("$.message").value(not(containsString("Private Desk"))));
    }

    @Test
    void anInvoiceCannotReferenceAnotherOrganisationsSubscription() throws Exception {
        UUID firstOwner = UUID.randomUUID();
        UUID secondOwner = UUID.randomUUID();
        UUID first = organisation(firstOwner, "invoice-a@example.com", "Invoice A");
        UUID second = organisation(secondOwner, "invoice-b@example.com", "Invoice B");
        UUID firstSubscription = subscriptionId(first);
        UUID secondSubscription = subscriptionId(second);

        assertThrows(DataAccessException.class, () -> jdbc.update("""
                insert into billing_invoices
                    (id, organisation_id, subscription_id, amount, currency, status, issued_on, plan_code, provider)
                values (?, ?, ?, 197.00, 'ZAR', 'DEMO', current_date, 'STARTER', 'demo')
                """, UUID.randomUUID(), first, secondSubscription));
        jdbc.update("""
                insert into billing_invoices
                    (id, organisation_id, subscription_id, amount, currency, status, issued_on, plan_code, provider)
                values (?, ?, ?, 197.00, 'ZAR', 'DEMO', current_date, 'STARTER', 'demo')
                """, UUID.randomUUID(), first, firstSubscription);
        assertEquals(1L, count("select count(*) from billing_invoices where organisation_id = ?", first));
    }

    @Test
    void successfulSimulatedCheckoutCreatesDemoRecordsOnly() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "demo-owner@example.com", "Demo Desk");

        mockMvc.perform(post("/api/billing/subscription").with(as(owner, "demo-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"planCode\":\"PROFESSIONAL\",\"outcome\":\"SUCCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("demo"))
                .andExpect(jsonPath("$.status").value("DEMO"))
                .andExpect(jsonPath("$.billingMode").value("DEMO"))
                .andExpect(jsonPath("$.liveChargesEnabled").value(false))
                .andExpect(jsonPath("$.plan.code").value("PROFESSIONAL"))
                .andExpect(jsonPath("$.detail").value(containsString("not a paid subscription")))
                .andExpect(jsonPath("$.history[0].status").value("DEMO"))
                .andExpect(jsonPath("$.history[0].provider").value("demo"))
                .andExpect(jsonPath("$.history[0].detail").value(containsString("No payment was collected")));

        assertEquals("DEMO", text("select status from organisation_subscriptions where organisation_id = ?", organisationId));
        assertEquals("demo", text("select provider from organisation_subscriptions where organisation_id = ?", organisationId));
        assertEquals(0L, count("""
                select count(*) from organisation_subscriptions
                where organisation_id = ? and status = 'ACTIVE'
                """, organisationId));
        assertEquals(0L, count("""
                select count(*) from billing_invoices
                where organisation_id = ? and status = 'PAID'
                """, organisationId));
        assertEquals("DEMO", text("select status from billing_invoices where organisation_id = ?", organisationId));
        assertEquals("demo", text("select provider from billing_invoices where organisation_id = ?", organisationId));
    }

    @Test
    void failedOrCanceledSimulatedCheckoutDoesNotActivateAPlan() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "fail-owner@example.com", "Fail Desk");

        mockMvc.perform(post("/api/billing/subscription").with(as(owner, "fail-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"planCode\":\"AGENCY\",\"outcome\":\"FAILED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("No package was activated")));
        mockMvc.perform(post("/api/billing/subscription").with(as(owner, "fail-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"planCode\":\"AGENCY\",\"outcome\":\"CANCELED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("canceled")));

        assertEquals("STARTER", text("""
                select plan.code from organisation_subscriptions subscription
                join subscription_plans plan on plan.id = subscription.plan_id
                where subscription.organisation_id = ?
                """, organisationId));
        assertEquals("DEMO", text("select status from organisation_subscriptions where organisation_id = ?", organisationId));
        assertEquals(0L, count("select count(*) from billing_invoices where organisation_id = ?", organisationId));
        assertEquals(0L, count("""
                select count(*) from organisation_subscriptions
                where organisation_id = ? and status in ('ACTIVE', 'PAID')
                """, organisationId));
    }

    @Test
    void simulatedCheckoutDoesNotRewriteANonDemoSubscription() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "paid-owner@example.com", "Paid Desk");
        jdbc.update("""
                update organisation_subscriptions
                set status = 'ACTIVE', provider = 'paystack'
                where organisation_id = ?
                """, organisationId);

        mockMvc.perform(post("/api/billing/subscription").with(as(owner, "paid-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content("{\"planCode\":\"GROWTH\",\"outcome\":\"SUCCESS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("not a demo")));
        assertEquals("ACTIVE", text("select status from organisation_subscriptions where organisation_id = ?", organisationId));
        assertEquals("paystack", text("select provider from organisation_subscriptions where organisation_id = ?", organisationId));
        assertEquals(0L, count("select count(*) from billing_invoices where organisation_id = ?", organisationId));
    }

    @Test
    void billingHistoryReturnsTheStoredProvider() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID organisationId = organisation(owner, "history-owner@example.com", "History Desk");
        UUID subscription = subscriptionId(organisationId);
        jdbc.update("""
                insert into billing_invoices
                    (id, organisation_id, subscription_id, amount, currency, status, issued_on, plan_code, provider)
                values (?, ?, ?, 494.00, 'ZAR', 'OPEN', current_date, 'GROWTH', 'paystack')
                """, UUID.randomUUID(), organisationId, subscription);

        mockMvc.perform(get("/api/billing/subscription").with(as(owner, "history-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history[0].provider").value("paystack"))
                .andExpect(jsonPath("$.history[0].status").value("OPEN"))
                .andExpect(jsonPath("$.history[0].detail").value("Billing record OPEN."));
    }

    private UUID organisation(UUID owner, String email, String name) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/organisations").with(as(owner, email))
                        .contentType("application/json")
                        .content("{\"name\":\"" + name + "\",\"displayName\":\"Owner\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String body = created.getResponse().getContentAsString();
        return UUID.fromString(body.replaceAll(".*\"organisationId\":\"([^\"]+)\".*", "$1"));
    }

    private void membership(UUID userId, UUID organisationId, String email, String role) {
        jdbc.update("insert into user_profiles (id, email, display_name) values (?, ?, ?)", userId, email, email);
        jdbc.update("""
                insert into organisation_memberships (id, organisation_id, user_id, role, status)
                values (?, ?, ?, ?, 'ACTIVE')
                """, UUID.randomUUID(), organisationId, userId, role);
    }

    private void createProperty(UUID owner, String email, UUID organisationId, String address) throws Exception {
        mockMvc.perform(post("/api/property").with(as(owner, email))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content(property(UUID.randomUUID(), address)))
                .andExpect(status().isCreated());
    }

    private int createPropertyStatus(UUID owner, UUID organisationId, String address) throws Exception {
        return mockMvc.perform(post("/api/property").with(as(owner, "property-owner@example.com"))
                        .header("X-Organisation-Id", organisationId.toString())
                        .contentType("application/json")
                        .content(property(UUID.randomUUID(), address)))
                .andReturn().getResponse().getStatus();
    }

    private String property(UUID id, String address) {
        return """
                {"propertyId":"%s","address":"%s","description":"Flat","rentalAmount":1000,"available":true}
                """.formatted(id, address);
    }

    private UUID subscriptionId(UUID organisationId) {
        return subscriptionId(organisationId, null);
    }

    private UUID subscriptionId(UUID organisationId, String status) {
        if (status == null) {
            return jdbc.queryForObject(
                    "select id from organisation_subscriptions where organisation_id = ? and status = 'DEMO'",
                    UUID.class,
                    organisationId);
        }
        return jdbc.queryForObject(
                "select id from organisation_subscriptions where organisation_id = ? and status = ?",
                UUID.class,
                organisationId,
                status);
    }

    private long count(String sql, Object... args) {
        Number value = jdbc.queryForObject(sql, Number.class, args);
        return value == null ? 0 : value.longValue();
    }

    private String text(String sql, Object... args) {
        return jdbc.queryForObject(sql, String.class, args);
    }

    private JwtRequestPostProcessor as(UUID userId, String email) {
        return jwt().jwt(token -> token.subject(userId.toString())
                .claim("email", email)
                .claim("email_verified", true));
    }

    private int[] race(ThrowingCall first, ThrowingCall second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> left = pool.submit(() -> first.call());
            Future<Integer> right = pool.submit(() -> second.call());
            return new int[] {left.get(30, TimeUnit.SECONDS), right.get(30, TimeUnit.SECONDS)};
        } finally {
            pool.shutdownNow();
        }
    }

    private int countStatus(int[] statuses, int expected) {
        int matches = 0;
        for (int status : statuses) {
            if (status == expected) {
                matches++;
            }
        }
        return matches;
    }

    @FunctionalInterface
    private interface ThrowingCall {
        int call() throws Exception;
    }
}
