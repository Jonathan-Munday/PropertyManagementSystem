package za.ac.mycput.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Portfolio API against PostgreSQL. Skipped when Docker is not available.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PropertyApiTest {

    static final UUID USER_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    static final UUID ORG_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

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

    @BeforeEach
    void membership() {
        PortfolioFixtures.membership(jdbc, USER_ID, ORG_ID, "Portico test");
    }

    @Test
    void healthIsPublicAndPortfolioRequiresAToken() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mockMvc.perform(get("/api/property")).andExpect(status().isUnauthorized());
    }

    @Test
    void updatingOnePropertyLeavesTheOtherInPlace() throws Exception {
        String first = "33333333-3333-4333-8333-333333333331";
        String second = "33333333-3333-4333-8333-333333333332";
        create(first, "1 Main Road", 8000, true);
        create(second, "2 Beach Road", 12000, false);

        mockMvc.perform(put("/api/property/" + first)
                        .with(member())
                        .contentType("application/json")
                        .content(propertyJson(first, "1 Main Road", 9500, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalAmount").value(9500.0));

        mockMvc.perform(get("/api/property/" + second).with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("2 Beach Road"))
                .andExpect(jsonPath("$.rentalAmount").value(12000.0));
    }

    @Test
    void subscriptionBillingStaysSeparateFromRent() throws Exception {
        mockMvc.perform(get("/api/billing/subscription").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("none"))
                .andExpect(jsonPath("$.status").value("NOT_CONFIGURED"));
    }

    @Test
    void openingALeaseOccupiesOnlyThatProperty() throws Exception {
        String first = "44444444-4444-4444-8444-444444444441";
        String second = "44444444-4444-4444-8444-444444444442";
        String tenantId = "66666666-6666-4666-8666-666666666661";
        String leaseId = "55555555-5555-4555-8555-555555555551";
        create(first, "1 Main Road", 8000, true);
        create(second, "2 Beach Road", 12000, true);
        mockMvc.perform(post("/api/tenant")
                        .with(member())
                        .contentType("application/json")
                        .content("""
                                {"tenantId":"%s","firstName":"Ada","lastName":"Lovelace","email":"ada.occupy@example.com","phoneNumber":"0210000000"}
                                """.formatted(tenantId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/lease")
                        .with(member())
                        .contentType("application/json")
                        .content("""
                                {"leaseId":"%s","propertyId":"%s","tenantId":"%s","tenantName":"Ada Lovelace","monthlyRent":8000,"startDate":"2020-01-01","endDate":"2099-12-31"}
                                """.formatted(leaseId, first, tenantId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(tenantId))
                .andExpect(jsonPath("$.startDate").value("2020-01-01"))
                .andExpect(jsonPath("$.endDate").value("2099-12-31"));

        mockMvc.perform(get("/api/property/" + first).with(member()))
                .andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(get("/api/property/" + second).with(member()))
                .andExpect(jsonPath("$.available").value(true));
        mockMvc.perform(delete("/api/lease/" + leaseId).with(member())).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/property/" + first).with(member()))
                .andExpect(jsonPath("$.available").value(true));
        mockMvc.perform(get("/api/property/" + second).with(member()))
                .andExpect(jsonPath("$.available").value(true));
    }

    private void create(String id, String address, int rent, boolean available) throws Exception {
        mockMvc.perform(post("/api/property")
                        .with(member())
                        .contentType("application/json")
                        .content(propertyJson(id, address, rent, available)))
                .andExpect(status().isCreated());
    }

    private String propertyJson(String id, String address, int rent, boolean available) {
        return """
                {"propertyId":"%s","address":"%s","description":"Flat","rentalAmount":%d,"available":%s}
                """.formatted(id, address, rent, available);
    }

    private JwtRequestPostProcessor member() {
        return jwt().jwt(token -> token.subject(USER_ID.toString()));
    }
}
