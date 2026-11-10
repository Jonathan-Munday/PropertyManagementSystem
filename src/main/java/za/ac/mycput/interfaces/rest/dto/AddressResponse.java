package za.ac.mycput.interfaces.rest.dto;

public record AddressResponse(
        String addressId,
        String streetAddress,
        String city,
        String province,
        String postalCode
) {
}
