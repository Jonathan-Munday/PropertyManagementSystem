package za.ac.mycput.interfaces.rest.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class PropertyRequest {

    @NotBlank
    private String propertyId;

    @NotBlank
    private String address;

    private String description;

    @NotNull
    @PositiveOrZero
    private Double rentalAmount;

    private Boolean available;

    private Boolean isAvailable;

    @AssertTrue(message = "is required")
    public boolean isAvailabilityPresent() {
        return available != null || isAvailable != null;
    }

    public boolean resolvedAvailable() {
        return available != null ? available : Boolean.TRUE.equals(isAvailable);
    }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getRentalAmount() { return rentalAmount; }
    public void setRentalAmount(Double rentalAmount) { this.rentalAmount = rentalAmount; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
