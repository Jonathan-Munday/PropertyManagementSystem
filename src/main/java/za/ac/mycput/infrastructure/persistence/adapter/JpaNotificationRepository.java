package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Notification;
import za.ac.mycput.domain.repository.INotificationRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.NotificationJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.NotificationPersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaNotificationRepository implements INotificationRepository {

    private final NotificationJpaRepository store;
    private final NotificationPersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaNotificationRepository(NotificationJpaRepository store,
                                     NotificationPersistenceMapper mapper,
                                     ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Notification create(Notification entity) {
        if (entity == null || entity.getNotificationId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getNotificationId());
        if (store.existsByOrganisationIdAndNotificationId(organisationId, id)) return null;
        NotificationJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Notification saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "notification", id);
        return saved;
    }

    @Override
    public Notification read(String notificationId) {
        if (notificationId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndNotificationId(persistence.organisationId(), Identifiers.parse(notificationId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Notification update(Notification entity) {
        if (entity == null || entity.getNotificationId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getNotificationId());
        NotificationJpaEntity existing = store.findByOrganisationIdAndNotificationId(organisationId, id).orElse(null);
        if (existing == null) return null;
        NotificationJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Notification saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "notification", id);
        return saved;
    }

    @Override
    public boolean delete(String notificationId) {
        if (notificationId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(notificationId);
        if (!store.existsByOrganisationIdAndNotificationId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndNotificationId(organisationId, id);
        persistence.audit("DELETE", "notification", id);
        return true;
    }

    @Override
    public List<Notification> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }
}
