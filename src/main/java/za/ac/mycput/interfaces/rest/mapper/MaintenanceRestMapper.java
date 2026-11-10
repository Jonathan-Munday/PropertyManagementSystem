package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.MaintenanceCommand;
import za.ac.mycput.domain.model.Maintenance;
import za.ac.mycput.interfaces.rest.dto.MaintenanceRequest;
import za.ac.mycput.interfaces.rest.dto.MaintenanceResponse;

@Component
public class MaintenanceRestMapper {

    public MaintenanceCommand toCommand(MaintenanceRequest request) {
        return new MaintenanceCommand(
                request.requestId(),
                request.description(),
                request.status(),
                request.reportedDate(),
                request.cost());
    }

    public MaintenanceResponse toResponse(Maintenance maintenance) {
        return new MaintenanceResponse(
                maintenance.getRequestId(),
                maintenance.getDescription(),
                maintenance.getStatus(),
                maintenance.getReportedDate(),
                maintenance.getCost());
    }
}
