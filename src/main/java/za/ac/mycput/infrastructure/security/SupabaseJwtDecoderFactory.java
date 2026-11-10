package za.ac.mycput.infrastructure.security;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Builds the resource-server decoder used for Supabase access tokens.
 *
 * <p>Signatures are verified by Nimbus JOSE + JWT through Spring Security.
 * This class does not implement cryptographic verification. Supported signing
 * algorithms are {@code RS256} and {@code ES256}, which are the asymmetric
 * keys published by Supabase JWT signing keys. HMAC ({@code HS256}) tokens,
 * including the legacy shared JWT secret, are rejected.
 *
 * <p>The JSON Web Key Set is loaded from the configured JWKS URI and cached by
 * Nimbus. A token whose {@code kid} is not in that cache causes a refresh, so
 * a newly published signing key is picked up without a restart. A revoked key
 * can remain acceptable until that cache refreshes, so leave the previous key
 * in the Supabase JWKS until outstanding access tokens have expired, then
 * revoke it. See {@code docs/authentication.md}.
 */
public final class SupabaseJwtDecoderFactory {

    private SupabaseJwtDecoderFactory() {
    }

    public static JwtDecoder create(String jwkSetUri, String issuer, String audience) {
        if (jwkSetUri == null || jwkSetUri.isBlank()) {
            throw new IllegalStateException("Supabase JWKS URI is required");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("Supabase JWT issuer is required");
        }
        if (audience == null || audience.isBlank()) {
            throw new IllegalStateException("Supabase JWT audience is required");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithms(algorithms -> {
                    algorithms.add(SignatureAlgorithm.RS256);
                    algorithms.add(SignatureAlgorithm.ES256);
                })
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new AudienceValidator(audience)));
        return decoder;
    }
}
