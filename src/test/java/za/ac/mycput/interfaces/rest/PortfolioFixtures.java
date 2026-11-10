package za.ac.mycput.interfaces.rest;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

final class PortfolioFixtures {

    private PortfolioFixtures() {
    }

    static void membership(JdbcTemplate jdbc, UUID userId, UUID organisationId, String name) {
        membership(jdbc, userId, organisationId, name, "OWNER");
    }

    static void membership(JdbcTemplate jdbc, UUID userId, UUID organisationId, String name, String role) {
        jdbc.update("insert into organisations (id, name) values (?, ?) on conflict (id) do nothing", organisationId, name);
        jdbc.update("insert into user_profiles (id, display_name) values (?, ?) on conflict (id) do nothing", userId, name);
        jdbc.update("""
                insert into organisation_memberships (id, organisation_id, user_id, role, status)
                values (?, ?, ?, ?, 'ACTIVE')
                on conflict (organisation_id, user_id) do nothing
                """, UUID.randomUUID(), organisationId, userId, role);
    }
}
