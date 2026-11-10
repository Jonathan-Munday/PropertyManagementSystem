package za.ac.mycput.application.dto;

public record AddressCommand(
        String addressId,
        String streetAddress,
        String city,
        String province,
        String postalCode
) {
}
