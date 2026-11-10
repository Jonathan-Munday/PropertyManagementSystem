package za.ac.mycput.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

/**
 * Rejects a database URL before the pool connects and before Flyway runs.
 * The JDBC URL is never included in the failure message, because it may contain a password.
 */
@Component
public class DatasourceStartupGuard implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DatasourceStartupGuard.class);
    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "0.0.0.0", "::1", "[::1]");

    private final Environment environment;
    private final boolean allowLocal;
    private final boolean localProfile;

    public DatasourceStartupGuard(Environment environment,
                                  @Value("${portico.datasource.allow-local:false}") boolean allowLocal) {
        this.environment = environment;
        this.allowLocal = allowLocal;
        this.localProfile = environment.matchesProfiles("local");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof HikariDataSource dataSource) {
            JdbcTarget target = validate(dataSource.getJdbcUrl(), allowLocal);
            if (localProfile && target.supabase()) {
                throw new IllegalStateException(
                        "The local Spring profile must not be used with Supabase. Start the API without that profile.");
            }
            String username = environment.getProperty("spring.datasource.username");
            checkCredentials(target, username, environment.getProperty("spring.datasource.password"));
            log.info("Database target host={} port={} database={} username={} ssl={}",
                    target.host(), target.port(), target.database(), username, target.requiresSsl());
        }
        return bean;
    }

    static JdbcTarget validate(String jdbcUrl, boolean allowLocal) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalStateException(
                    "SPRING_DATASOURCE_URL is required. Portico has no localhost database fallback.");
        }
        JdbcTarget target = parse(jdbcUrl);
        if (target.embeddedCredentials()) {
            throw new IllegalStateException(
                    "Put the database user and password in SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD. Do not embed them in the JDBC URL.");
        }
        if (target.local() && !allowLocal) {
            throw new IllegalStateException(
                    "Refusing a localhost database. Use the Supabase session pooler or direct connection from the dashboard. "
                            + "Set PORTICO_DATASOURCE_ALLOW_LOCAL=true only for an intentional private PostgreSQL.");
        }
        if (target.supabase() && target.port() == 6543) {
            throw new IllegalStateException(
                    "The Supabase transaction pooler on port 6543 is not supported for Hibernate and Flyway. "
                            + "Use the session pooler or the direct connection, both on port 5432, with sslmode=require.");
        }
        if (target.supabase() && !target.requiresSsl()) {
            throw new IllegalStateException(
                    "A Supabase JDBC URL must include sslmode=require. The database host was '" + target.host() + "'.");
        }
        return target;
    }

    static void checkCredentials(JdbcTarget target, String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException(
                    "SPRING_DATASOURCE_USERNAME is required. Portico has no default database user.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "SPRING_DATASOURCE_PASSWORD is required. Portico has no default database password.");
        }
        if (target.pooler() && "postgres".equalsIgnoreCase(username)) {
            throw new IllegalStateException(
                    "The Supabase session pooler at '" + target.host()
                            + "' requires the username postgres.<project-ref>. The username 'postgres' is only for a direct database connection.");
        }
        if (target.direct() && username.indexOf('.') >= 0) {
            throw new IllegalStateException(
                    "The direct Supabase host '" + target.host()
                            + "' requires the username postgres. Do not use the session-pooler username here.");
        }
    }

    private static JdbcTarget parse(String jdbcUrl) {
        if (!jdbcUrl.regionMatches(true, 0, "jdbc:postgresql:", 0, "jdbc:postgresql:".length())) {
            throw new IllegalStateException("SPRING_DATASOURCE_URL must be a PostgreSQL JDBC URL.");
        }
        URI uri;
        try {
            uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("SPRING_DATASOURCE_URL is not a valid PostgreSQL JDBC URL.");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("SPRING_DATASOURCE_URL must include a database host.");
        }
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
        return new JdbcTarget(host, port, database, uri.getUserInfo() != null, uri.getRawQuery());
    }

    record JdbcTarget(String host, int port, String database, boolean embeddedCredentials, String query) {

        boolean local() {
            return LOCAL_HOSTS.contains(host.toLowerCase(Locale.ROOT));
        }

        boolean supabase() {
            String normalised = host.toLowerCase(Locale.ROOT);
            return normalised.endsWith(".supabase.co")
                    || normalised.equals("supabase.co")
                    || pooler();
        }

        boolean pooler() {
            String normalised = host.toLowerCase(Locale.ROOT);
            return normalised.endsWith(".pooler.supabase.com") || normalised.equals("pooler.supabase.com");
        }

        boolean direct() {
            String normalised = host.toLowerCase(Locale.ROOT);
            return normalised.startsWith("db.") && normalised.endsWith(".supabase.co");
        }

        boolean requiresSsl() {
            if (query == null || query.isBlank()) {
                return false;
            }
            for (String part : query.split("&")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2 && pair[0].equalsIgnoreCase("sslmode")) {
                    String mode = pair[1].toLowerCase(Locale.ROOT);
                    return mode.equals("require") || mode.equals("verify-ca") || mode.equals("verify-full");
                }
            }
            return false;
        }
    }
}
