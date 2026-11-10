package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Maintenance;
import za.ac.mycput.domain.model.MaintenanceStatus;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.MaintenanceJpaEntity;

@Component
public class MaintenancePersistenceMapper {

    public MaintenanceJpaEntity toEntity(Maintenance maintenance) {
        MaintenanceJpaEntity entity = new MaintenanceJpaEntity();
        entity.setRequestId(Identifiers.parse(maintenance.getRequestId()));
        entity.setDescription(maintenance.getDescription());
        entity.setStatus(MaintenanceStatus.parse(maintenance.getStatus()).name());
        entity.setReportedDate(maintenance.getReportedDate());
        entity.setCost(MoneyColumns.fromDouble(maintenance.getCost()));
        return entity;
    }

    public Maintenance toDomain(MaintenanceJpaEntity entity) {
        return new Maintenance.Builder()
                .setRequestId(Identifiers.format(entity.getRequestId()))
                .setDescription(entity.getDescription())
                .setStatus(entity.getStatus())
                .setReportedDate(entity.getReportedDate())
                .setCost(MoneyColumns.toDouble(entity.getCost()))
                .build();
    }
}
