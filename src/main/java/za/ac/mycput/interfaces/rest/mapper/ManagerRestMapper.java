package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.ManagerCommand;
import za.ac.mycput.domain.model.Manager;
import za.ac.mycput.interfaces.rest.dto.ManagerRequest;
import za.ac.mycput.interfaces.rest.dto.ManagerResponse;

@Component
public class ManagerRestMapper {

    public ManagerCommand toCommand(ManagerRequest request) {
        return new ManagerCommand(
                request.managerId(),
                request.name(),
                request.contactNumber(),
                request.employeeId());
    }

    public ManagerResponse toResponse(Manager manager) {
        return new ManagerResponse(
                manager.getManagerId(),
                manager.getName(),
                manager.getContactNumber(),
                manager.getEmployeeId());
    }
}
