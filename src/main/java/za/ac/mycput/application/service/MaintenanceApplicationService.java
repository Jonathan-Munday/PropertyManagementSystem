package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.MaintenanceCommand;
import za.ac.mycput.application.usecase.OpenMaintenanceRequestUseCase;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.EntityNotFoundException;
import za.ac.mycput.domain.factory.MaintenanceFactory;
import za.ac.mycput.domain.model.Maintenance;
import za.ac.mycput.domain.repository.IMaintenanceRepository;
import za.ac.mycput.domain.service.MaintenanceCostPolicy;

import java.util.List;

@Service
public class MaintenanceApplicationService {

    private final IMaintenanceRepository maintenance;
    private final OpenMaintenanceRequestUseCase openRequest;
    private final MaintenanceCostPolicy costs;

    public MaintenanceApplicationService(IMaintenanceRepository maintenance,
                                         OpenMaintenanceRequestUseCase openRequest,
                                         MaintenanceCostPolicy costs) {
        this.maintenance = maintenance;
        this.openRequest = openRequest;
        this.costs = costs;
    }

    @Transactional
    public List<Maintenance> list() {
        return maintenance.findAll();
    }

    @Transactional
    public Maintenance get(String requestId) {
        Maintenance found = maintenance.findById(requestId);
        if (found == null) {
            throw new EntityNotFoundException("Maintenance", requestId);
        }
        return found;
    }

    public Maintenance open(MaintenanceCommand command) {
        return openRequest.execute(command);
    }

    @Transactional
    public Maintenance update(String requestId, MaintenanceCommand command) {
        if (!requestId.equals(command.requestId())) {
            throw new DomainRuleException("The maintenance id in the path and body must match");
        }
        get(requestId);
        costs.assertValid(command.description(), command.status(), command.cost());
        Maintenance updated = RepositoryOutcomes.built(
                MaintenanceFactory.createMaintenance(
                        requestId, command.description(), command.status(), command.reportedDate(), command.cost()),
                "A maintenance request is incomplete");
        Maintenance saved = maintenance.update(updated);
        if (saved == null) {
            throw new EntityNotFoundException("Maintenance", requestId);
        }
        return saved;
    }

    @Transactional
    public void delete(String requestId) {
        get(requestId);
        maintenance.delete(requestId);
    }
}
