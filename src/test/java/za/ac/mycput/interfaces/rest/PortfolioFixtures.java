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

    static void agencyDemo(JdbcTemplate jdbc, UUID organisationId) {
        Number open = jdbc.queryForObject("""
                select count(*) from organisation_subscriptions
                where organisation_id = ? and status in ('TRIALING', 'ACTIVE', 'PAST_DUE', 'DEMO')
                """, Number.class, organisationId);
        if (open != null && open.longValue() > 0) {
            return;
        }
        UUID planId = jdbc.queryForObject("select id from subscription_plans where code = 'AGENCY'", UUID.class);
        jdbc.update("""
                insert into organisation_subscriptions (id, organisation_id, plan_id, status, provider)
                values (?, ?, ?, 'DEMO', 'demo')
                """, UUID.randomUUID(), organisationId, planId);
    }
}
