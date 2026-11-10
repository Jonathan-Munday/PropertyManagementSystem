package za.ac.mycput.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.MaintenanceCommand;
import za.ac.mycput.application.service.RepositoryOutcomes;
import za.ac.mycput.domain.factory.MaintenanceFactory;
import za.ac.mycput.domain.model.Maintenance;
import za.ac.mycput.domain.repository.IMaintenanceRepository;
import za.ac.mycput.domain.service.MaintenanceCostPolicy;

@Service
public class OpenMaintenanceRequestUseCase {

    private final IMaintenanceRepository maintenance;
    private final MaintenanceCostPolicy costs;

    public OpenMaintenanceRequestUseCase(IMaintenanceRepository maintenance, MaintenanceCostPolicy costs) {
        this.maintenance = maintenance;
        this.costs = costs;
    }

    @Transactional
    public Maintenance execute(MaintenanceCommand command) {
        costs.assertValid(command.description(), command.status(), command.cost());
        if (command.reportedDate() == null) {
            throw new za.ac.mycput.domain.exception.DomainRuleException("A maintenance request needs a reported date");
        }
        Maintenance request = RepositoryOutcomes.built(
                MaintenanceFactory.createMaintenance(
                        command.requestId(),
                        command.description(),
                        command.status(),
                        command.reportedDate(),
                        command.cost()),
                "A maintenance request is incomplete");
        if (maintenance.findById(request.getRequestId()) != null) {
            throw new za.ac.mycput.domain.exception.DuplicateEntityException("Maintenance", request.getRequestId());
        }
        return maintenance.save(request);
    }
}
