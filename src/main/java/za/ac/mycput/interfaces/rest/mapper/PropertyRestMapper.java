package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.PropertyCommand;
import za.ac.mycput.domain.model.Property;
import za.ac.mycput.interfaces.rest.dto.PropertyRequest;
import za.ac.mycput.interfaces.rest.dto.PropertyResponse;

@Component
public class PropertyRestMapper {

    public PropertyCommand toCommand(PropertyRequest request) {
        return new PropertyCommand(
                request.getPropertyId(),
                request.getAddress(),
                request.getDescription(),
                request.getRentalAmount(),
                request.resolvedAvailable());
    }

    public PropertyResponse toResponse(Property property) {
        return new PropertyResponse(
                property.getPropertyId(),
                property.getAddress(),
                property.getDescription(),
                property.getRentalAmount(),
                property.isAvailable());
    }
}
