package za.ac.mycput.domain.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Lease terms are calendar dates in South Africa. Johannesburg has no daylight
 * saving, so a date rolls over at 00:00 SAST (UTC+02:00) on the server and in
 * the browser. Instants elsewhere in the API stay in UTC.
 */
public final class BusinessCalendar {

    public static final ZoneId ZONE = ZoneId.of("Africa/Johannesburg");

    private BusinessCalendar() {
    }

    public static LocalDate today(Clock clock) {
        return LocalDate.now(clock.withZone(ZONE));
    }
}
