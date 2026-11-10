package za.ac.mycput.interfaces.rest.dto;

public record NotificationResponse(
        String notificationId,
        String message,
        String type,
        String dateSent,
        String tenantId
) {
}
