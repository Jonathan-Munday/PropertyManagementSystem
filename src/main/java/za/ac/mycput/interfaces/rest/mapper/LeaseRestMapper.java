package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.LeaseCommand;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.interfaces.rest.dto.LeaseRequest;
import za.ac.mycput.interfaces.rest.dto.LeaseResponse;

@Component
public class LeaseRestMapper {

    public LeaseCommand toCommand(LeaseRequest request) {
        return new LeaseCommand(
                request.leaseId(),
                request.propertyId(),
                request.tenantName(),
                request.monthlyRent(),
                request.tenantId(),
                request.startDate(),
                request.endDate());
    }

    public LeaseResponse toResponse(Lease lease) {
        return new LeaseResponse(
                lease.getLeaseId(),
                lease.getPropertyId(),
                lease.getTenantName(),
                lease.getMonthlyRent(),
                lease.getTenantId(),
                lease.getStartDate(),
                lease.getEndDate());
    }
}
