package za.ac.mycput.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsOriginsTest {

    @Test
    void aMissingOriginFailsClosed() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> CorsOrigins.parse(" "));
        assertTrue(error.getMessage().contains("PORTICO_CORS_ORIGINS"));
        assertTrue(error.getMessage().contains("localhost:5173"));
    }

    @Test
    void aWildcardIsRejected() {
        assertThrows(IllegalStateException.class, () -> CorsOrigins.parse("*"));
        assertThrows(IllegalStateException.class, () -> CorsOrigins.parse("https://portico.example, *"));
    }

    @Test
    void explicitOriginsAreKept() {
        assertEquals(
                List.of("https://portico.example", "http://localhost:5173"),
                CorsOrigins.parse(" https://portico.example, http://localhost:5173 "));
    }
}
