package za.ac.mycput.interfaces.rest.dto;

import java.time.LocalDate;

public record PaymentResponse(
        String paymentId,
        String leaseId,
        double amount,
        LocalDate paymentDate,
        String paymentMethod
) {
}
