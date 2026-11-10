package za.ac.mycput.domain.service;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.exception.DomainRuleException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaseTermConstraintTest {

    private final LeaseTermPolicy terms = new LeaseTermPolicy();

    @Test
    void migrationAllowsBothNullOrACompleteOrderedTermOnly() throws IOException {
        String sql = new String(
                LeaseTermConstraintTest.class.getResourceAsStream("/db/migration/V2__lease_term_dates.sql")
                        .readAllBytes(),
                StandardCharsets.UTF_8);

        assertTrue(sql.contains("(start_date is null and end_date is null)"));
        assertTrue(sql.contains("start_date is not null"));
        assertTrue(sql.contains("end_date is not null"));
        assertTrue(sql.contains("end_date >= start_date"));
        assertFalse(sql.contains("start_date is null or end_date is null or end_date >= start_date"));

        assertTrue(constraintAllows(null, null));
        assertTrue(constraintAllows(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        assertTrue(constraintAllows(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)));
        assertFalse(constraintAllows(LocalDate.of(2026, 1, 1), null));
        assertFalse(constraintAllows(null, LocalDate.of(2026, 12, 31)));
        assertFalse(constraintAllows(LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)));
    }

    @Test
    void applicationWritesRejectAPartialOrReversedTerm() {
        assertEquals("A lease needs a start date",
                assertThrows(DomainRuleException.class, () -> terms.assertComplete(null, LocalDate.of(2026, 12, 31))).getMessage());
        assertEquals("A lease needs an end date",
                assertThrows(DomainRuleException.class, () -> terms.assertComplete(LocalDate.of(2026, 1, 1), null)).getMessage());
        assertEquals("The end date cannot be before the start date",
                assertThrows(DomainRuleException.class,
                        () -> terms.assertComplete(LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1))).getMessage());
    }

    @Test
    void leaseTodayFollowsJohannesburgNotUtc() {
        Instant justBeforeMidnightInJohannesburg = Instant.parse("2026-11-09T22:30:00Z");

        assertEquals(LocalDate.of(2026, 11, 10),
                BusinessCalendar.today(java.time.Clock.fixed(justBeforeMidnightInJohannesburg, ZoneOffset.UTC)));
        assertEquals(LocalDate.of(2026, 11, 9),
                LocalDate.now(java.time.Clock.fixed(justBeforeMidnightInJohannesburg, ZoneOffset.UTC)));
    }

    private static boolean constraintAllows(LocalDate start, LocalDate end) {
        boolean bothNull = start == null && end == null;
        boolean bothPresentAndOrdered = start != null && end != null && !end.isBefore(start);
        return bothNull || bothPresentAndOrdered;
    }
}
