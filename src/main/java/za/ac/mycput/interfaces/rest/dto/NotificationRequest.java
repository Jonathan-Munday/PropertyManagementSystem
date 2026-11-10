package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record NotificationRequest(
        @NotBlank String notificationId,
        @NotBlank String message,
        String type,
        String dateSent,
        @NotBlank String tenantId
) {
}
