package za.ac.mycput.application.dto;

import java.time.LocalDate;

public record PaymentCommand(
        String paymentId,
        String leaseId,
        double amount,
        LocalDate paymentDate,
        String paymentMethod
) {
}
