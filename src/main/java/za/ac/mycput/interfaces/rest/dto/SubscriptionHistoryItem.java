package za.ac.mycput.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionHistoryItem(
        String planCode,
        String status,
        String provider,
        BigDecimal amount,
        LocalDate issuedOn,
        String detail
) {
}
