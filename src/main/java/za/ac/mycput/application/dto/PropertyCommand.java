package za.ac.mycput.application.dto;

public record PropertyCommand(
        String propertyId,
        String address,
        String description,
        double rentalAmount,
        boolean available
) {
}
