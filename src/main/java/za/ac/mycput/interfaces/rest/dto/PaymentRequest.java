package za.ac.mycput.interfaces.rest.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record PaymentRequest(
        @NotBlank String paymentId,
        @NotBlank String leaseId,
        @NotNull @Positive Double amount,
        @NotNull LocalDate paymentDate,
        @NotBlank String paymentMethod
) {
}
