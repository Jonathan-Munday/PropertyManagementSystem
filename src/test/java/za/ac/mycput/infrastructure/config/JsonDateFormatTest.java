package za.ac.mycput.infrastructure.config;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JsonDateFormatTest {

    @Test
    void jackson3WritesDatesAsIsoStringsByDefault() {
        assertFalse(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS.enabledByDefault());
        JsonMapper mapper = JsonMapper.builder().build();
        assertEquals("\"2026-11-05\"", mapper.writeValueAsString(LocalDate.of(2026, 11, 5)));
        assertEquals("\"2026-11-05T12:00:00Z\"", mapper.writeValueAsString(Instant.parse("2026-11-05T12:00:00Z")));
    }
}
