package za.ac.mycput.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import za.ac.mycput.domain.access.OrganisationRole;
import za.ac.mycput.domain.factory.PropertyFactory;
import za.ac.mycput.domain.repository.IPropertyRepository;
import za.ac.mycput.infrastructure.security.OrganisationContext;
import za.ac.mycput.infrastructure.security.OrganisationScope;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PortfolioPersistenceTest {

    private static final UUID USER_A = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1");
    private static final UUID ORG_A = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2");
    private static final UUID USER_B = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1");
    private static final UUID ORG_B = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb2");

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

    @Autowired
    private IPropertyRepository properties;

    @BeforeEach
    void memberships() {
        PortfolioFixtures.membership(jdbc, USER_A, ORG_A, "Organisation A");
        PortfolioFixtures.membership(jdbc, USER_B, ORG_B, "Organisation B");
    }

    @Test
    void crudCoversThePortfolioResources() throws Exception {
        String propertyId = "c1000000-0000-4000-8000-000000000001";
        String tenantId = "c2000000-0000-4000-8000-000000000001";
        String leaseId = "c3000000-0000-4000-8000-000000000001";
        String paymentId = "c4000000-0000-4000-8000-000000000001";
        String requestId = "c5000000-0000-4000-8000-000000000001";
        String managerId = "c6000000-0000-4000-8000-000000000001";
        String agentId = "c7000000-0000-4000-8000-000000000001";

        mockMvc.perform(post("/api/property").with(as(USER_A)).contentType("application/json")
                        .content(property(propertyId, "1 Harbour Road", 9000, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.propertyId").value(propertyId));
        mockMvc.perform(put("/api/property/" + propertyId).with(as(USER_A)).contentType("application/json")
                        .content(property(propertyId, "1 Harbour Road", 9500, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalAmount").value(9500.0));

        mockMvc.perform(post("/api/tenant").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"tenantId":"%s","firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","phoneNumber":"0210000000"}
                                """.formatted(tenantId)))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/tenant/" + tenantId).with(as(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@example.com"));

        mockMvc.perform(post("/api/lease").with(as(USER_A)).contentType("application/json")
                        .content(lease(leaseId, propertyId, tenantId, 9500, "2026-01-01", "2099-12-31")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(tenantId))
                .andExpect(jsonPath("$.startDate").value("2026-01-01"))
                .andExpect(jsonPath("$.endDate").value("2099-12-31"));
        mockMvc.perform(get("/api/lease/" + leaseId).with(as(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenantId))
                .andExpect(jsonPath("$.propertyId").value(propertyId))
                .andExpect(jsonPath("$.monthlyRent").value(9500.0))
                .andExpect(jsonPath("$.startDate").value("2026-01-01"))
                .andExpect(jsonPath("$.endDate").value("2099-12-31"));
        mockMvc.perform(get("/api/property/" + propertyId).with(as(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(post("/api/payment").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"paymentId":"%s","leaseId":"%s","amount":9500,"paymentDate":"2026-11-01","paymentMethod":"EFT"}
                                """.formatted(paymentId, leaseId)))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/payment/" + paymentId).with(as(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(9500.0));

        mockMvc.perform(post("/api/maintenance").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"requestId":"%s","description":"Leaking tap","status":"OPEN","reportedDate":"2026-11-01","cost":0}
                                """.formatted(requestId)))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/maintenance/" + requestId).with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"requestId":"%s","description":"Leaking tap","status":"IN_PROGRESS","reportedDate":"2026-11-01","cost":150.50}
                                """.formatted(requestId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/manager").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"managerId":"%s","name":"Grace Hopper","contactNumber":"0211111111","employeeId":"EMP-1"}
                                """.formatted(managerId)))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/manager").with(as(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Grace Hopper"));

        mockMvc.perform(post("/api/agent").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"agentId":"%s","name":"Alan Turing","email":"alan@example.com","role":"Letting"}
                                """.formatted(agentId)))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/agent/" + agentId).with(as(USER_A))).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/agent/" + agentId).with(as(USER_A))).andExpect(status().isNotFound());
    }

    @Test
    void relationshipsAndInvalidDataAreRejected() throws Exception {
        String propertyId = "d1000000-0000-4000-8000-000000000001";
        String tenantId = "d2000000-0000-4000-8000-000000000001";
        String leaseId = "d3000000-0000-4000-8000-000000000001";
        String paymentId = "d4000000-0000-4000-8000-000000000001";
        mockMvc.perform(post("/api/property").with(as(USER_A)).contentType("application/json")
                        .content(property(propertyId, "9 Long Street", 1000, true)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tenant").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"tenantId":"%s","firstName":"Grace","lastName":"Hopper","email":"grace@example.com","phoneNumber":"0211111111"}
                                """.formatted(tenantId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/lease").with(as(USER_A)).contentType("application/json")
                        .content(lease(leaseId, propertyId, tenantId, 1000, "2026-01-01", "2099-12-31")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/property/" + propertyId).with(as(USER_A))).andExpect(status().isConflict());
        mockMvc.perform(post("/api/payment").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"paymentId":"%s","leaseId":"%s","amount":1000,"paymentDate":"2026-11-02","paymentMethod":"EFT"}
                                """.formatted(paymentId, leaseId)))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/lease/" + leaseId).with(as(USER_A))).andExpect(status().isConflict());
        mockMvc.perform(get("/api/property/" + propertyId).with(as(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(get("/api/lease/" + leaseId).with(as(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenantId));

        mockMvc.perform(post("/api/property").with(as(USER_A)).contentType("application/json")
                        .content(property(propertyId, "", -1, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/property").with(as(USER_A)).contentType("application/json")
                        .content(property("not-a-uuid", "1 Main Road", 1000, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DOMAIN_RULE"));
        mockMvc.perform(post("/api/maintenance").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"requestId":"d5000000-0000-4000-8000-000000000001","description":"Door","status":"BROKEN","reportedDate":"2026-11-02","cost":10}
                                """))
                .andExpect(status().isBadRequest());

        UUID missingLease = UUID.fromString("d9000000-0000-4000-8000-000000000099");
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                insert into rent_payments (payment_id, organisation_id, lease_id, amount, payment_date, payment_method)
                values (?, ?, ?, 10.00, '2026-11-02', 'EFT')
                """, UUID.randomUUID(), ORG_A, missingLease));
    }

    @Test
    void oneOrganisationCannotReadAnotherOrganisationsProperty() throws Exception {
        String propertyId = "e1000000-0000-4000-8000-000000000001";
        mockMvc.perform(post("/api/property").with(as(USER_A)).contentType("application/json")
                        .content(property(propertyId, "Secret Street", 5000, true)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/property/" + propertyId).with(as(USER_B))).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/property").with(as(USER_B)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].propertyId", not(hasItem(propertyId))));
    }

    @Test
    void repositoryAdapterDoesNotReturnAnotherOrganisationsRow() {
        String propertyId = "e2000000-0000-4000-8000-000000000002";
        OrganisationContext.set(new OrganisationScope(USER_A, ORG_A, OrganisationRole.OWNER));
        try {
            properties.create(PropertyFactory.createProperty(propertyId, "Scoped Road", "Flat", 4000, true));
            OrganisationContext.set(new OrganisationScope(USER_B, ORG_B, OrganisationRole.OWNER));
            assertNull(properties.read(propertyId));
            assertEquals(0, properties.getAll().stream().filter(item -> propertyId.equals(item.getPropertyId())).count());
        } finally {
            OrganisationContext.clear();
        }
    }

    @Test
    void rowLevelSecurityDeniesDirectTableAccess() {
        List<String> tables = List.of(
                "organisations", "user_profiles", "organisation_memberships", "organisation_invitations",
                "addresses", "properties", "tenants", "leases", "rent_payments", "maintenance_requests",
                "managers", "agents", "vendors", "notifications", "subscription_plans",
                "organisation_subscriptions", "billing_invoices", "billing_webhook_events", "audit_logs");
        for (String table : tables) {
            Boolean enabled = jdbc.queryForObject("""
                    select c.relrowsecurity
                    from pg_class c
                    join pg_namespace n on n.oid = c.relnamespace
                    where n.nspname = 'public' and c.relname = ?
                    """, Boolean.class, table);
            Integer policies = jdbc.queryForObject("""
                    select count(*)
                    from pg_policies
                    where schemaname = 'public' and tablename = ?
                    """, Integer.class, table);
            assertEquals(Boolean.TRUE, enabled, table);
            assertEquals(0, policies, table);
        }
    }

    @Test
    void leaseTermIsScopedAndALegacyRowKeepsNullDates() throws Exception {
        String propertyId = "f1000000-0000-4000-8000-000000000001";
        String otherPropertyId = "f1000000-0000-4000-8000-000000000002";
        String tenantId = "f2000000-0000-4000-8000-000000000001";
        String otherTenantId = "f2000000-0000-4000-8000-000000000002";
        String leaseId = "f3000000-0000-4000-8000-000000000001";
        String legacyLeaseId = "f3000000-0000-4000-8000-000000000002";

        mockMvc.perform(post("/api/property").with(as(USER_A)).contentType("application/json")
                        .content(property(propertyId, "1 Lease Lane", 8000, true)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/property").with(as(USER_B)).contentType("application/json")
                        .content(property(otherPropertyId, "Secret Lease Lane", 8000, true)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tenant").with(as(USER_A)).contentType("application/json")
                        .content("""
                                {"tenantId":"%s","firstName":"Ada","lastName":"Lovelace","email":"ada.lease@example.com","phoneNumber":"0210000001"}
                                """.formatted(tenantId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/tenant").with(as(USER_B)).contentType("application/json")
                        .content("""
                                {"tenantId":"%s","firstName":"Alan","lastName":"Turing","email":"alan.lease@example.com","phoneNumber":"0210000002"}
                                """.formatted(otherTenantId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/lease").with(as(USER_A)).contentType("application/json")
                        .content(lease(leaseId, otherPropertyId, tenantId, 8000, "2026-01-01", "2099-12-31")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/lease").with(as(USER_A)).contentType("application/json")
                        .content(lease(leaseId, propertyId, otherTenantId, 8000, "2026-01-01", "2099-12-31")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/lease").with(as(USER_A)).contentType("application/json")
                        .content(lease(leaseId, propertyId, tenantId, 8000, "2026-05-01", "2026-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DOMAIN_RULE"));

        mockMvc.perform(post("/api/lease").with(as(USER_A)).contentType("application/json")
                        .content(lease(leaseId, propertyId, tenantId, 8000, "2026-01-01", "2099-12-31")))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/lease/" + leaseId).with(as(USER_A)).contentType("application/json")
                        .content(lease(leaseId, propertyId, tenantId, 8100, "2026-02-01", "2099-11-30")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRent").value(8100.0))
                .andExpect(jsonPath("$.tenantId").value(tenantId))
                .andExpect(jsonPath("$.propertyId").value(propertyId))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"));
        mockMvc.perform(put("/api/lease/" + leaseId).with(as(USER_A)).contentType("application/json")
                        .content(lease(leaseId, otherPropertyId, tenantId, 8100, "2026-02-01", "2099-11-30")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DOMAIN_RULE"));
        mockMvc.perform(get("/api/lease/" + leaseId).with(as(USER_B))).andExpect(status().isNotFound());

        jdbc.update("""
                insert into leases (lease_id, organisation_id, property_id, tenant_name, monthly_rent)
                values (?, ?, ?, 'Legacy Tenant', 1500.00)
                """, UUID.fromString(legacyLeaseId), ORG_A, UUID.fromString(propertyId));
        mockMvc.perform(get("/api/lease/" + legacyLeaseId).with(as(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantName").value("Legacy Tenant"))
                .andExpect(jsonPath("$.startDate").value(nullValue()))
                .andExpect(jsonPath("$.endDate").value(nullValue()));
        jdbc.update("""
                update leases set start_date = '2024-01-01', end_date = '2024-12-31' where lease_id = ?
                """, UUID.fromString(legacyLeaseId));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                update leases set end_date = null where lease_id = ?
                """, UUID.fromString(legacyLeaseId)));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                update leases set start_date = null where lease_id = ?
                """, UUID.fromString(legacyLeaseId)));

        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                update leases set start_date = '2026-12-31', end_date = '2026-01-01' where lease_id = ?
                """, UUID.fromString(leaseId)));

        mockMvc.perform(delete("/api/lease/" + leaseId).with(as(USER_A))).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/property/" + propertyId).with(as(USER_A)))
                .andExpect(jsonPath("$.available").value(false));
    }

    private JwtRequestPostProcessor as(UUID userId) {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }

    private String lease(String leaseId, String propertyId, String tenantId, int rent, String start, String end) {
        return """
                {"leaseId":"%s","propertyId":"%s","tenantId":"%s","tenantName":"Ada Lovelace","monthlyRent":%d,"startDate":"%s","endDate":"%s"}
                """.formatted(leaseId, propertyId, tenantId, rent, start, end);
    }

    private String property(String id, String address, int rent, boolean available) {
        return """
                {"propertyId":"%s","address":"%s","description":"Flat","rentalAmount":%d,"available":%s}
                """.formatted(id, address, rent, available);
    }
}
