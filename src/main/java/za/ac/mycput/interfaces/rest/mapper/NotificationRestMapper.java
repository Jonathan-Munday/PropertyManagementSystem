package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.NotificationCommand;
import za.ac.mycput.domain.model.Notification;
import za.ac.mycput.interfaces.rest.dto.NotificationRequest;
import za.ac.mycput.interfaces.rest.dto.NotificationResponse;

@Component
public class NotificationRestMapper {

    public NotificationCommand toCommand(NotificationRequest request) {
        return new NotificationCommand(
                request.notificationId(),
                request.message(),
                request.type(),
                request.dateSent(),
                request.tenantId());
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getMessage(),
                notification.getType(),
                notification.getDateSent(),
                notification.getTenantId());
    }
}
