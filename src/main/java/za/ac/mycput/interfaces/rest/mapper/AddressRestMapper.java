package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.AddressCommand;
import za.ac.mycput.domain.model.Address;
import za.ac.mycput.interfaces.rest.dto.AddressRequest;
import za.ac.mycput.interfaces.rest.dto.AddressResponse;

@Component
public class AddressRestMapper {

    public AddressCommand toCommand(AddressRequest request) {
        return new AddressCommand(
                request.addressId(),
                request.streetAddress(),
                request.city(),
                request.province(),
                request.postalCode());
    }

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getAddressId(),
                address.getStreetAddress(),
                address.getCity(),
                address.getProvince(),
                address.getPostalCode());
    }
}
