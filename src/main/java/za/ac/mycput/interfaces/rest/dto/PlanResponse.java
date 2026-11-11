package za.ac.mycput.interfaces.rest.dto;

import java.math.BigDecimal;

public record PlanResponse(
        String code,
        String name,
        BigDecimal amount,
        String currency,
        String interval,
        int maxProperties,
        int maxUsers
) {
}
