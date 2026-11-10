package za.ac.mycput.interfaces.rest.dto;

public record PropertyResponse(
        String propertyId,
        String address,
        String description,
        double rentalAmount,
        boolean available
) {
}
