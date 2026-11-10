package za.ac.mycput.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasourceStartupGuardTest {

    @Test
    void rejectsAMissingUrl() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> DatasourceStartupGuard.validate("  ", false));
        assertTrue(error.getMessage().contains("SPRING_DATASOURCE_URL"));
    }

    @Test
    void rejectsLocalhostUnlessTheDeveloperOptsIn() {
        assertThrows(IllegalStateException.class,
                () -> DatasourceStartupGuard.validate("jdbc:postgresql://localhost:5432/portico", false));
        assertThrows(IllegalStateException.class,
                () -> DatasourceStartupGuard.validate("jdbc:postgresql://127.0.0.1:5432/portico", false));
        assertDoesNotThrow(() -> DatasourceStartupGuard.validate(
                "jdbc:postgresql://localhost:5432/portico", true));
    }

    @Test
    void rejectsCredentialsEmbeddedInTheUrl() {
        assertThrows(IllegalStateException.class, () -> DatasourceStartupGuard.validate(
                "jdbc:postgresql://portico:secret@aws-0-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require",
                false));
    }

    @Test
    void acceptsASupabaseSessionPoolerWithSsl() {
        assertDoesNotThrow(() -> DatasourceStartupGuard.validate(
                "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require",
                false));
    }

    @Test
    void acceptsASupabaseDirectConnectionWithSsl() {
        assertDoesNotThrow(() -> DatasourceStartupGuard.validate(
                "jdbc:postgresql://db.example.supabase.co:5432/postgres?sslmode=verify-full",
                false));
    }

    @Test
    void rejectsSupabaseWithoutSslAndTheTransactionPooler() {
        assertThrows(IllegalStateException.class, () -> DatasourceStartupGuard.validate(
                "jdbc:postgresql://db.example.supabase.co:5432/postgres",
                false));
        assertThrows(IllegalStateException.class, () -> DatasourceStartupGuard.validate(
                "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:6543/postgres?sslmode=require",
                false));
        assertThrows(IllegalStateException.class, () -> DatasourceStartupGuard.validate(
                "jdbc:postgresql://db.example.supabase.co:5432/postgres?sslmode=disable",
                false));
    }

    @Test
    void matchesTheUsernameToTheConnectionMethod() {
        var pooler = DatasourceStartupGuard.validate(
                "jdbc:postgresql://aws-1-eu-west-3.pooler.supabase.com:5432/postgres?sslmode=require",
                false);
        var direct = DatasourceStartupGuard.validate(
                "jdbc:postgresql://db.example.supabase.co:5432/postgres?sslmode=require",
                false);

        assertThrows(IllegalStateException.class,
                () -> DatasourceStartupGuard.checkCredentials(pooler, "postgres", "present"));
        assertDoesNotThrow(() -> DatasourceStartupGuard.checkCredentials(
                pooler, "postgres.exampleproject", "present"));
        assertThrows(IllegalStateException.class,
                () -> DatasourceStartupGuard.checkCredentials(direct, "postgres.exampleproject", "present"));
        assertDoesNotThrow(() -> DatasourceStartupGuard.checkCredentials(direct, "postgres", "present"));
        assertThrows(IllegalStateException.class,
                () -> DatasourceStartupGuard.checkCredentials(pooler, "postgres.exampleproject", " "));
    }
}
