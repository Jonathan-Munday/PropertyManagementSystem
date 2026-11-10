package za.ac.mycput.infrastructure.security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies Supabase access tokens with the production decoder. No shared HMAC secret is used.
 */
class SupabaseJwtDecoderTest {

    static final String ISSUER = "https://test-project.supabase.co/auth/v1";
    static final String AUDIENCE = "authenticated";

    static JwksServer server;
    static JwtDecoder decoder;

    @BeforeAll
    static void keys() throws Exception {
        server = JwksServer.start();
        decoder = SupabaseJwtDecoderFactory.create(server.uri(), ISSUER, AUDIENCE);
    }

    @AfterAll
    static void stop() {
        if (server != null) {
            server.close();
        }
    }

    @Test
    void acceptsRs256AndEs256TokensSignedByThePublishedKey() throws Exception {
        UUID subject = UUID.randomUUID();
        RSAKey rsa = new RSAKeyGenerator(2048).keyID("rsa-1").generate();
        server.publish(rsa);
        Jwt decoded = decoder.decode(sign(rsa, JWSAlgorithm.RS256, claims(subject.toString(), Instant.now().plusSeconds(300))));
        assertEquals(subject.toString(), decoded.getSubject());
        assertEquals(ISSUER, decoded.getIssuer().toString());
        assertEquals(AUDIENCE, decoded.getAudience().get(0));

        ECKey ec = new ECKeyGenerator(Curve.P_256).keyID("ec-1").algorithm(JWSAlgorithm.ES256).generate();
        server.publish(ec);
        Jwt elliptic = decoder.decode(sign(ec, JWSAlgorithm.ES256, claims(subject.toString(), Instant.now().plusSeconds(300))));
        assertEquals(subject.toString(), elliptic.getSubject());
    }

    @Test
    void rejectsExpiredWrongIssuerWrongAudienceBadSignatureAndHmacTokens() throws Exception {
        RSAKey key = new RSAKeyGenerator(2048).keyID("rsa-reject").generate();
        server.publish(key);
        Instant now = Instant.now();
        assertThrows(JwtException.class, () -> decoder.decode(sign(key, JWSAlgorithm.RS256, claims(UUID.randomUUID().toString(), now.minusSeconds(300)))));
        assertThrows(JwtException.class, () -> decoder.decode(sign(key, JWSAlgorithm.RS256, claims(UUID.randomUUID().toString(), now.plusSeconds(300), "https://other.example/auth/v1", AUDIENCE))));
        assertThrows(JwtException.class, () -> decoder.decode(sign(key, JWSAlgorithm.RS256, claims(UUID.randomUUID().toString(), now.plusSeconds(300), ISSUER, "anon"))));

        RSAKey other = new RSAKeyGenerator(2048).keyID(key.getKeyID()).generate();
        assertThrows(JwtException.class, () -> decoder.decode(sign(other, JWSAlgorithm.RS256, claims(UUID.randomUUID().toString(), now.plusSeconds(300)))));

        SignedJWT hmac = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                claims(UUID.randomUUID().toString(), now.plusSeconds(300)));
        hmac.sign(new MACSigner("local-dev-only-change-this-secret-key-now-32b".getBytes()));
        assertThrows(JwtException.class, () -> decoder.decode(hmac.serialize()));
    }

    @Test
    void refreshesTheJwksWhenARotatedSigningKeyAppears() throws Exception {
        RSAKey first = new RSAKeyGenerator(2048).keyID("rotate-a").generate();
        RSAKey second = new RSAKeyGenerator(2048).keyID("rotate-b").generate();
        server.publish(first);
        String subject = UUID.randomUUID().toString();
        assertEquals(subject, decoder.decode(sign(first, JWSAlgorithm.RS256, claims(subject, Instant.now().plusSeconds(300)))).getSubject());

        server.publish(second);
        assertEquals(subject, decoder.decode(sign(second, JWSAlgorithm.RS256, claims(subject, Instant.now().plusSeconds(300)))).getSubject());
    }

    @Test
    void factoryRejectsAMissingIssuer() {
        assertThrows(IllegalStateException.class, () -> SupabaseJwtDecoderFactory.create(server.uri(), " ", AUDIENCE));
    }

    private static JWTClaimsSet claims(String subject, Instant expiresAt) {
        return claims(subject, expiresAt, ISSUER, AUDIENCE);
    }

    private static JWTClaimsSet claims(String subject, Instant expiresAt, String issuer, String audience) {
        Instant issuedAt = expiresAt.minusSeconds(60);
        return new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .audience(audience)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim("email", "ada@example.com")
                .claim("email_verified", true)
                .build();
    }

    private static String sign(JWK key, JWSAlgorithm algorithm, JWTClaimsSet claims) throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(algorithm).keyID(key.getKeyID()).type(JOSEObjectType.JWT).build(),
                claims);
        if (key instanceof RSAKey rsa) {
            jwt.sign(new RSASSASigner(rsa));
        } else if (key instanceof ECKey ec) {
            jwt.sign(new ECDSASigner(ec));
        } else {
            throw new IllegalArgumentException("Unsupported test key");
        }
        return jwt.serialize();
    }
}
