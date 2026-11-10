package za.ac.mycput.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "properties")
public class PropertyJpaEntity extends OrganisationOwnedEntity {

    @Id
    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "address", length = 500, nullable = false)
    private String address;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "rental_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal rentalAmount;

    @Column(name = "available", nullable = false)
    private boolean available;

    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getRentalAmount() { return rentalAmount; }
    public void setRentalAmount(BigDecimal rentalAmount) { this.rentalAmount = rentalAmount; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
