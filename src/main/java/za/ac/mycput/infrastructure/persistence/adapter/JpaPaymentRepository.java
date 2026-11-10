package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.model.Payment;
import za.ac.mycput.domain.repository.IPaymentRepository;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.entity.PaymentJpaEntity;
import za.ac.mycput.infrastructure.persistence.mapper.PaymentPersistenceMapper;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class JpaPaymentRepository implements IPaymentRepository {

    private final PaymentJpaRepository store;
    private final PaymentPersistenceMapper mapper;
    private final ScopedPersistence persistence;

    public JpaPaymentRepository(PaymentJpaRepository store, PaymentPersistenceMapper mapper, ScopedPersistence persistence) {
        this.store = store;
        this.mapper = mapper;
        this.persistence = persistence;
    }

    @Override
    public Payment create(Payment entity) {
        if (entity == null || entity.getPaymentId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getPaymentId());
        if (store.existsByOrganisationIdAndPaymentId(organisationId, id)) return null;
        PaymentJpaEntity row = mapper.toEntity(entity);
        row.markCreated(organisationId);
        Payment saved = mapper.toDomain(store.save(row));
        persistence.audit("CREATE", "rent_payment", id);
        return saved;
    }

    @Override
    public Payment read(String paymentId) {
        if (paymentId == null) return null;
        persistence.bind();
        return store.findByOrganisationIdAndPaymentId(persistence.organisationId(), Identifiers.parse(paymentId))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Payment update(Payment entity) {
        if (entity == null || entity.getPaymentId() == null) return null;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(entity.getPaymentId());
        PaymentJpaEntity existing = store.findByOrganisationIdAndPaymentId(organisationId, id).orElse(null);
        if (existing == null) return null;
        PaymentJpaEntity row = mapper.toEntity(entity);
        row.markUpdated(organisationId, existing.getCreatedAt());
        Payment saved = mapper.toDomain(store.save(row));
        persistence.audit("UPDATE", "rent_payment", id);
        return saved;
    }

    @Override
    public boolean delete(String paymentId) {
        if (paymentId == null) return false;
        persistence.bind();
        UUID organisationId = persistence.organisationId();
        UUID id = Identifiers.parse(paymentId);
        if (!store.existsByOrganisationIdAndPaymentId(organisationId, id)) return false;
        store.deleteByOrganisationIdAndPaymentId(organisationId, id);
        persistence.audit("DELETE", "rent_payment", id);
        return true;
    }

    @Override
    public List<Payment> getAll() {
        persistence.bind();
        return store.findByOrganisationId(persistence.organisationId()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Payment> findByPaymentMethod(String paymentMethod) {
        if (paymentMethod == null) return List.of();
        persistence.bind();
        return store.findByOrganisationIdAndPaymentMethodIgnoreCase(persistence.organisationId(), paymentMethod)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
