package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.PropertyCommand;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.factory.PropertyFactory;
import za.ac.mycput.domain.model.Property;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPropertyRepository;
import za.ac.mycput.domain.service.BusinessCalendar;
import za.ac.mycput.domain.service.PropertyOccupancyPolicy;
import za.ac.mycput.domain.service.RentalAmountPolicy;

import java.time.Clock;
import java.util.List;

@Service
public class PropertyApplicationService {

    private final IPropertyRepository properties;
    private final ILeaseRepository leases;
    private final RentalAmountPolicy rentalAmounts;
    private final PropertyOccupancyPolicy occupancy;
    private final Clock clock;
    private final SubscriptionAllowance subscriptions;

    public PropertyApplicationService(IPropertyRepository properties,
                                      ILeaseRepository leases,
                                      RentalAmountPolicy rentalAmounts,
                                      PropertyOccupancyPolicy occupancy,
                                      Clock clock,
                                      SubscriptionAllowance subscriptions) {
        this.properties = properties;
        this.leases = leases;
        this.rentalAmounts = rentalAmounts;
        this.occupancy = occupancy;
        this.clock = clock;
        this.subscriptions = subscriptions;
    }

    @Transactional
    public List<Property> list(Boolean availableOnly) {
        if (Boolean.TRUE.equals(availableOnly)) {
            return properties.findAvailable();
        }
        return properties.getAll();
    }

    @Transactional
    public Property get(String propertyId) {
        return RepositoryOutcomes.existing(properties.read(propertyId), "Property", propertyId);
    }

    @Transactional
    public Property create(PropertyCommand command) {
        subscriptions.assertCanAddProperty();
        Property property = build(command);
        RepositoryOutcomes.absent(properties.read(property.getPropertyId()), "Property", property.getPropertyId());
        return RepositoryOutcomes.created(properties.create(property), "Property", property.getPropertyId());
    }

    @Transactional
    public Property update(String propertyId, PropertyCommand command) {
        if (!propertyId.equals(command.propertyId())) {
            throw new DomainRuleException("The property id in the path and body must match");
        }
        RepositoryOutcomes.existing(properties.read(propertyId), "Property", propertyId);
        Property property = build(command);
        occupancy.assertAvailability(property, leases.getAll(), BusinessCalendar.today(clock));
        return RepositoryOutcomes.updated(properties.update(property), "Property", propertyId);
    }

    @Transactional
    public void delete(String propertyId) {
        RepositoryOutcomes.deleted(properties.delete(propertyId), "Property", propertyId);
    }

    private Property build(PropertyCommand command) {
        rentalAmounts.assertNonNegative(command.rentalAmount());
        return RepositoryOutcomes.built(
                PropertyFactory.createProperty(
                        command.propertyId(),
                        command.address(),
                        command.description(),
                        command.rentalAmount(),
                        command.available()),
                "A property needs an id and an address");
    }
}
