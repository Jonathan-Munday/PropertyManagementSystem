package za.ac.mycput.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Applies V3 to a database that already has V1 and V2 data.
 * Skipped when Docker is not available. Does not connect to Supabase.
 */
@Testcontainers(disabledWithoutDocker = true)
class SubscriptionMigrationTest {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Test
    void v3BackfillsStarterWithoutDamagingExistingRows() {
        DataSource dataSource = dataSource();
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target("2")
                .load()
                .migrate();

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        UUID legacyOrganisation = UUID.randomUUID();
        UUID legacyUser = UUID.randomUUID();
        UUID legacyProperty = UUID.randomUUID();
        UUID canceledOrganisation = UUID.randomUUID();
        UUID existingStarter = UUID.randomUUID();
        jdbc.update("""
                insert into subscription_plans (id, code, name, amount, currency, interval_unit)
                values (?, 'STARTER', 'Starter', 197.00, 'ZAR', 'MONTH')
                """, existingStarter);
        jdbc.update("insert into organisations (id, name) values (?, ?)", legacyOrganisation, "Legacy Desk");
        jdbc.update("insert into user_profiles (id, email, display_name) values (?, ?, ?)",
                legacyUser, "legacy@example.com", "Legacy Owner");
        jdbc.update("""
                insert into organisation_memberships (id, organisation_id, user_id, role, status)
                values (?, ?, ?, 'OWNER', 'ACTIVE')
                """, UUID.randomUUID(), legacyOrganisation, legacyUser);
        jdbc.update("""
                insert into properties (property_id, organisation_id, address, description, rental_amount, available)
                values (?, ?, '1 Legacy Road', 'Flat', 2500, true)
                """, legacyProperty, legacyOrganisation);
        jdbc.update("insert into organisations (id, name) values (?, ?)", canceledOrganisation, "Canceled Desk");
        UUID canceledSubscription = UUID.randomUUID();
        jdbc.update("""
                insert into organisation_subscriptions (id, organisation_id, plan_id, status, provider)
                values (?, ?, ?, 'CANCELED', 'paystack')
                """, canceledSubscription, canceledOrganisation, existingStarter);

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        assertEquals(5, limit(jdbc, "STARTER", "max_properties"));
        assertEquals(2, limit(jdbc, "STARTER", "max_users"));
        assertEquals(25, limit(jdbc, "GROWTH", "max_properties"));
        assertEquals(8, limit(jdbc, "GROWTH", "max_users"));
        assertEquals(80, limit(jdbc, "PROFESSIONAL", "max_properties"));
        assertEquals(20, limit(jdbc, "PROFESSIONAL", "max_users"));
        assertEquals(300, limit(jdbc, "AGENCY", "max_properties"));
        assertEquals(60, limit(jdbc, "AGENCY", "max_users"));
        assertEquals(197.0, amount(jdbc, "STARTER"));
        assertEquals(1979.0, amount(jdbc, "AGENCY"));

        assertEquals(1L, count(jdbc, "select count(*) from properties where property_id = ?", legacyProperty));
        assertEquals(1L, count(jdbc, """
                select count(*) from organisation_memberships
                where organisation_id = ? and user_id = ? and status = 'ACTIVE'
                """, legacyOrganisation, legacyUser));
        assertEquals(1L, count(jdbc, "select count(*) from subscription_plans where code = 'STARTER'"));
        assertEquals(existingStarter, jdbc.queryForObject(
                "select id from subscription_plans where code = 'STARTER'", UUID.class));
        assertEquals(1L, count(jdbc, """
                select count(*) from organisation_subscriptions subscription
                join subscription_plans plan on plan.id = subscription.plan_id
                where subscription.organisation_id = ?
                  and subscription.status = 'DEMO'
                  and subscription.provider = 'demo'
                  and plan.code = 'STARTER'
                """, legacyOrganisation));
        assertEquals("CANCELED", jdbc.queryForObject(
                "select status from organisation_subscriptions where id = ?", String.class, canceledSubscription));
        assertEquals("paystack", jdbc.queryForObject(
                "select provider from organisation_subscriptions where id = ?", String.class, canceledSubscription));
        assertEquals(1L, count(jdbc, "select count(*) from organisation_subscriptions where organisation_id = ?", canceledOrganisation));
        assertEquals(0L, count(jdbc, "select count(*) from billing_invoices where status = 'PAID'"));

        UUID legacySubscription = jdbc.queryForObject(
                "select id from organisation_subscriptions where organisation_id = ?", UUID.class, legacyOrganisation);
        assertThrows(DataAccessException.class, () -> jdbc.update("""
                insert into billing_invoices
                    (id, organisation_id, subscription_id, amount, currency, status, issued_on, plan_code, provider)
                values (?, ?, ?, 10.00, 'ZAR', 'DEMO', current_date, 'STARTER', 'demo')
                """, UUID.randomUUID(), canceledOrganisation, legacySubscription));
    }

    private static DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        return dataSource;
    }

    private static int limit(JdbcTemplate jdbc, String code, String column) {
        Integer value = jdbc.queryForObject(
                "select " + column + " from subscription_plans where code = ?", Integer.class, code);
        return value == null ? -1 : value;
    }

    private static double amount(JdbcTemplate jdbc, String code) {
        Number value = jdbc.queryForObject(
                "select amount from subscription_plans where code = ?", Number.class, code);
        return value == null ? -1 : value.doubleValue();
    }

    private static long count(JdbcTemplate jdbc, String sql, Object... args) {
        Number value = jdbc.queryForObject(sql, Number.class, args);
        return value == null ? 0 : value.longValue();
    }
}
