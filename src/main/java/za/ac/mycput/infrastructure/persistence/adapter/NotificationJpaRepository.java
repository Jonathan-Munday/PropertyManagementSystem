package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.NotificationJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {
    Optional<NotificationJpaEntity> findByOrganisationIdAndNotificationId(UUID organisationId, UUID notificationId);
    List<NotificationJpaEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationIdAndNotificationId(UUID organisationId, UUID notificationId);
    void deleteByOrganisationIdAndNotificationId(UUID organisationId, UUID notificationId);
}
