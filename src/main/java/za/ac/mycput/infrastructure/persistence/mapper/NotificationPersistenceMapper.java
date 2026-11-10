package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Notification;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.NotificationJpaEntity;

@Component
public class NotificationPersistenceMapper {

    public NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setNotificationId(Identifiers.parse(notification.getNotificationId()));
        entity.setTenantId(Identifiers.parse(notification.getTenantId()));
        entity.setMessage(notification.getMessage());
        entity.setType(notification.getType());
        entity.setDateSent(notification.getDateSent());
        return entity;
    }

    public Notification toDomain(NotificationJpaEntity entity) {
        return new Notification.Builder()
                .setNotificationId(Identifiers.format(entity.getNotificationId()))
                .setTenantId(Identifiers.format(entity.getTenantId()))
                .setMessage(entity.getMessage())
                .setType(entity.getType())
                .setDateSent(entity.getDateSent())
                .build();
    }
}
