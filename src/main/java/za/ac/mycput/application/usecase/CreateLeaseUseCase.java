package za.ac.mycput.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.LeaseCommand;
import za.ac.mycput.application.service.PropertyAvailability;
import za.ac.mycput.application.service.RepositoryOutcomes;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.EntityNotFoundException;
import za.ac.mycput.domain.factory.LeaseFactory;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.domain.model.Property;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPropertyRepository;
import za.ac.mycput.domain.service.LeaseTermPolicy;
import za.ac.mycput.domain.service.PropertyOccupancyPolicy;

@Service
public class CreateLeaseUseCase {

    private final IPropertyRepository properties;
    private final ILeaseRepository leases;
    private final PropertyOccupancyPolicy occupancy;
    private final LeaseTermPolicy terms;
    private final PropertyAvailability availability;

    public CreateLeaseUseCase(IPropertyRepository properties,
                               ILeaseRepository leases,
                               PropertyOccupancyPolicy occupancy,
                               LeaseTermPolicy terms,
                               PropertyAvailability availability) {
        this.properties = properties;
        this.leases = leases;
        this.occupancy = occupancy;
        this.terms = terms;
        this.availability = availability;
    }

    @Transactional
    public Lease execute(LeaseCommand command) {
        if (command.propertyId() == null || command.propertyId().isBlank()) {
            throw new DomainRuleException("A lease needs a property");
        }
        if (command.tenantId() == null || command.tenantId().isBlank()) {
            throw new DomainRuleException("A lease needs a tenant");
        }
        terms.assertComplete(command.startDate(), command.endDate());
        if (!properties.lock(command.propertyId())) {
            throw new EntityNotFoundException("Property", command.propertyId());
        }
        Property property = RepositoryOutcomes.existing(
                properties.read(command.propertyId()), "Property", command.propertyId());
        Lease lease = RepositoryOutcomes.built(
                LeaseFactory.createLease(
                        command.leaseId(),
                        command.propertyId(),
                        command.tenantId(),
                        command.tenantName(),
                        command.monthlyRent(),
                        command.startDate(),
                        command.endDate()),
                "A lease needs an id, a property, a tenant name, and a rent that is not negative");
        occupancy.assertCanOpen(property, lease, leases.getAll());
        RepositoryOutcomes.absent(leases.read(lease.getLeaseId()), "Lease", lease.getLeaseId());
        Lease saved = RepositoryOutcomes.created(leases.create(lease), "Lease", lease.getLeaseId());
        availability.reconcile(property.getPropertyId());
        return saved;
    }
}
