package za.ac.mycput.application.dto;

public record NotificationCommand(
        String notificationId,
        String message,
        String type,
        String dateSent,
        String tenantId
) {
}
