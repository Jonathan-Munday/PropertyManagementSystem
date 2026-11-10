package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.*;

public record VendorRequest(
        @NotBlank String vendorId,
        @NotBlank String companyName,
        @NotBlank String contactNumber,
        @NotBlank String specialty
) {
}
