package za.ac.mycput.application.service;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.factory.PropertyFactory;
import za.ac.mycput.domain.model.Property;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPropertyRepository;
import za.ac.mycput.domain.service.BusinessCalendar;
import za.ac.mycput.domain.service.PropertyOccupancyPolicy;

import java.time.Clock;

/**
 * Aligns the stored availability flag with leases that still occupy a property.
 * The flag is updated in the same transaction as the lease write. Opening a
 * lease does not trust a stale flag; that decision uses the lease terms.
 */
@Component
public class PropertyAvailability {

    private final IPropertyRepository properties;
    private final ILeaseRepository leases;
    private final PropertyOccupancyPolicy occupancy;
    private final Clock clock;

    public PropertyAvailability(IPropertyRepository properties,
                                ILeaseRepository leases,
                                PropertyOccupancyPolicy occupancy,
                                Clock clock) {
        this.properties = properties;
        this.leases = leases;
        this.occupancy = occupancy;
        this.clock = clock;
    }

    public void reconcile(String propertyId) {
        Property property = RepositoryOutcomes.existing(properties.read(propertyId), "Property", propertyId);
        boolean occupied = occupancy.isOccupied(propertyId, leases.getAll(), BusinessCalendar.today(clock));
        if (property.isAvailable() == !occupied) {
            return;
        }
        Property updated = RepositoryOutcomes.built(
                PropertyFactory.createProperty(
                        property.getPropertyId(),
                        property.getAddress(),
                        property.getDescription(),
                        property.getRentalAmount(),
                        !occupied),
                "A property needs an id and an address");
        RepositoryOutcomes.updated(properties.update(updated), "Property", propertyId);
    }
}
