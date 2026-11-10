package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.LeaseCommand;
import za.ac.mycput.application.usecase.CreateLeaseUseCase;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.EntityNotFoundException;
import za.ac.mycput.domain.factory.LeaseFactory;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPropertyRepository;
import za.ac.mycput.domain.repository.ITenantRepository;
import za.ac.mycput.domain.service.LeaseTermPolicy;
import za.ac.mycput.domain.service.PropertyOccupancyPolicy;
import za.ac.mycput.domain.valueobject.Money;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaseApplicationService {

    private final ILeaseRepository leases;
    private final ITenantRepository tenants;
    private final IPropertyRepository properties;
    private final CreateLeaseUseCase createLease;
    private final LeaseTermPolicy terms;
    private final PropertyOccupancyPolicy occupancy;
    private final PropertyAvailability availability;

    public LeaseApplicationService(ILeaseRepository leases,
                                   ITenantRepository tenants,
                                   IPropertyRepository properties,
                                   CreateLeaseUseCase createLease,
                                   LeaseTermPolicy terms,
                                   PropertyOccupancyPolicy occupancy,
                                   PropertyAvailability availability) {
        this.leases = leases;
        this.tenants = tenants;
        this.properties = properties;
        this.createLease = createLease;
        this.terms = terms;
        this.occupancy = occupancy;
        this.availability = availability;
    }

    @Transactional
    public List<Lease> list() {
        return leases.getAll();
    }

    @Transactional
    public Lease get(String leaseId) {
        return RepositoryOutcomes.existing(leases.read(leaseId), "Lease", leaseId);
    }

    @Transactional
    public Lease create(LeaseCommand command) {
        requireTenant(command.tenantId());
        return createLease.execute(command);
    }

    @Transactional
    public Lease update(String leaseId, LeaseCommand command) {
        if (!leaseId.equals(command.leaseId())) {
            throw new DomainRuleException("The lease id in the path and body must match");
        }
        Lease current = RepositoryOutcomes.existing(leases.read(leaseId), "Lease", leaseId);
        if (!current.getPropertyId().equals(command.propertyId())) {
            throw new DomainRuleException("A lease cannot be moved to another property");
        }
        lockProperty(current.getPropertyId());
        if (!Money.zar(command.monthlyRent()).isPositive()) {
            throw new DomainRuleException("Monthly rent must be positive");
        }
        String tenantId = command.tenantId();
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = current.getTenantId();
        }
        requireTenant(tenantId);
        LocalDate startDate = command.startDate() != null ? command.startDate() : current.getStartDate();
        LocalDate endDate = command.endDate() != null ? command.endDate() : current.getEndDate();
        if (startDate != null || endDate != null) {
            terms.assertComplete(startDate, endDate);
        }
        Lease updated = RepositoryOutcomes.built(
                LeaseFactory.createLease(
                        leaseId,
                        current.getPropertyId(),
                        tenantId,
                        command.tenantName(),
                        command.monthlyRent(),
                        startDate,
                        endDate),
                "A lease needs an id, a property, a tenant name, and a rent that is not negative");
        occupancy.assertTermIsFree(updated, leases.getAll());
        Lease saved = RepositoryOutcomes.updated(leases.update(updated), "Lease", leaseId);
        availability.reconcile(current.getPropertyId());
        return saved;
    }

    private void requireTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new DomainRuleException("A lease needs a tenant");
        }
        String id = tenantId.trim();
        RepositoryOutcomes.existing(tenants.read(id), "Tenant", id);
    }

    @Transactional
    public void delete(String leaseId) {
        Lease current = RepositoryOutcomes.existing(leases.read(leaseId), "Lease", leaseId);
        lockProperty(current.getPropertyId());
        RepositoryOutcomes.deleted(leases.delete(leaseId), "Lease", leaseId);
        availability.reconcile(current.getPropertyId());
    }

    private void lockProperty(String propertyId) {
        if (!properties.lock(propertyId)) {
            throw new EntityNotFoundException("Property", propertyId);
        }
    }
}
