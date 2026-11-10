package za.ac.mycput.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import za.ac.mycput.application.service.AccountApplicationService;
import za.ac.mycput.domain.service.RolePermissionPolicy;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, OrganisationAccessFilter organisationAccessFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .addFilterAfter(organisationAccessFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    OrganisationAccessFilter organisationAccessFilter(OrganisationAccessService access,
                                                      AccountApplicationService accounts,
                                                      ApiAuthorisationCatalog catalog,
                                                      RolePermissionPolicy policy,
                                                      @Value("${portico.tenancy.local-access:false}") boolean localAccess,
                                                      @Value("${portico.tenancy.local-user-id:00000000-0000-0000-0000-000000000001}") String localUserId,
                                                      @Value("${portico.tenancy.local-organisation-id:00000000-0000-0000-0000-000000000010}") String localOrganisationId) {
        return new OrganisationAccessFilter(access, accounts, catalog, policy, localAccess, localUserId, localOrganisationId);
    }

    /**
     * Keeps this filter out of the servlet container. It is added once, after bearer-token
     * authentication, by {@link #securityFilterChain}. Enabling this registration would run it twice.
     */
    @Bean
    FilterRegistrationBean<OrganisationAccessFilter> organisationAccessFilterRegistration(OrganisationAccessFilter filter) {
        FilterRegistrationBean<OrganisationAccessFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${portico.security.jwt.jwk-set-uri}") String jwkSetUri,
                          @Value("${portico.security.jwt.issuer}") String issuer,
                          @Value("${portico.security.jwt.audience}") String audience) {
        return SupabaseJwtDecoderFactory.create(jwkSetUri, issuer, audience);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${portico.security.cors.allowed-origins}") String origins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Organisation-Id"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
