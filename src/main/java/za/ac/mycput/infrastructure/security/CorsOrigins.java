package za.ac.mycput.infrastructure.security;

import java.util.Arrays;
import java.util.List;

/**
 * Explicit browser origins for the API.
 * A blank value fails startup. Production does not fall back to the Vite dev server.
 */
public final class CorsOrigins {

    private CorsOrigins() {
    }

    public static List<String> parse(String configured) {
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException(
                    "PORTICO_CORS_ORIGINS is required. Portico will not fall back to http://localhost:5173.");
        }
        List<String> origins = Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        if (origins.isEmpty() || origins.stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalStateException(
                    "PORTICO_CORS_ORIGINS must list explicit origins. A wildcard is not allowed.");
        }
        return origins;
    }
}
