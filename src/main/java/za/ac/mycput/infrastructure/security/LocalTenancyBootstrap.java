package za.ac.mycput.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Locale;
import java.util.UUID;

/**
 * Development-only identity rows so the React app can call the API without Supabase.
 * It does not insert portfolio records. It refuses a Supabase URL because that
 * database ties user_profiles.id to auth.users and this fixed user is not one of them.
 */
@Configuration
@ConditionalOnProperty(name = "portico.tenancy.local-access", havingValue = "true")
public class LocalTenancyBootstrap {

    @Bean
    ApplicationRunner localOrganisation(JdbcTemplate jdbc,
                                        Environment environment,
                                        @Value("${portico.tenancy.local-user-id}") String userId,
                                        @Value("${portico.tenancy.local-organisation-id}") String organisationId,
                                        @Value("${portico.tenancy.local-organisation-name:Local portfolio}") String organisationName) {
        return args -> {
            String url = environment.getProperty("spring.datasource.url", "");
            if (url.toLowerCase(Locale.ROOT).contains("supabase")) {
                throw new IllegalStateException(
                        "The local profile must not insert a development user into Supabase. Start without the local profile.");
            }
            UUID user = UUID.fromString(userId);
            UUID organisation = UUID.fromString(organisationId);
            jdbc.update("""
                    insert into organisations (id, name)
                    values (?, ?)
                    on conflict (id) do nothing
                    """, organisation, organisationName);
            jdbc.update("""
                    insert into user_profiles (id, display_name)
                    values (?, ?)
                    on conflict (id) do nothing
                    """, user, organisationName);
            jdbc.update("""
                    insert into organisation_memberships (id, organisation_id, user_id, role, status)
                    values (?, ?, ?, 'OWNER', 'ACTIVE')
                    on conflict (organisation_id, user_id) do nothing
                    """, UUID.randomUUID(), organisation, user);
        };
    }
}
