package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.TenantCommand;
import za.ac.mycput.domain.model.Tenant;
import za.ac.mycput.interfaces.rest.dto.TenantRequest;
import za.ac.mycput.interfaces.rest.dto.TenantResponse;

@Component
public class TenantRestMapper {

    public TenantCommand toCommand(TenantRequest request) {
        return new TenantCommand(
                request.tenantId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber());
    }

    public TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getTenantId(),
                tenant.getFirstName(),
                tenant.getLastName(),
                tenant.getEmail(),
                tenant.getPhoneNumber());
    }
}
