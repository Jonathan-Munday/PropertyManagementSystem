package za.ac.mycput.interfaces.rest.dto;

public record VendorResponse(
        String vendorId,
        String companyName,
        String contactNumber,
        String specialty
) {
}
