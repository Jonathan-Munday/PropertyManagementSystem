package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record AddressRequest(
        @NotBlank String addressId,
        @NotBlank String streetAddress,
        String city,
        String province,
        String postalCode
) {
}
