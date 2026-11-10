package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.LeaseJpaEntity;

@Component
public class LeasePersistenceMapper {

    public LeaseJpaEntity toEntity(Lease lease) {
        LeaseJpaEntity entity = new LeaseJpaEntity();
        entity.setLeaseId(Identifiers.parse(lease.getLeaseId()));
        entity.setPropertyId(Identifiers.parse(lease.getPropertyId()));
        if (lease.getTenantId() != null && !lease.getTenantId().isBlank()) {
            entity.setTenantId(Identifiers.parse(lease.getTenantId()));
        }
        entity.setTenantName(lease.getTenantName());
        entity.setMonthlyRent(MoneyColumns.fromDouble(lease.getMonthlyRent()));
        entity.setStartDate(lease.getStartDate());
        entity.setEndDate(lease.getEndDate());
        return entity;
    }

    public Lease toDomain(LeaseJpaEntity entity) {
        Lease.Builder builder = new Lease.Builder()
                .setLeaseId(Identifiers.format(entity.getLeaseId()))
                .setPropertyId(Identifiers.format(entity.getPropertyId()))
                .setTenantName(entity.getTenantName())
                .setMonthlyRent(MoneyColumns.toDouble(entity.getMonthlyRent()))
                .setStartDate(entity.getStartDate())
                .setEndDate(entity.getEndDate());
        if (entity.getTenantId() != null) {
            builder.setTenantId(Identifiers.format(entity.getTenantId()));
        }
        return builder.build();
    }
}
