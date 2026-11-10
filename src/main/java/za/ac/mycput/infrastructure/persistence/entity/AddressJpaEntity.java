package za.ac.mycput.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "addresses")
public class AddressJpaEntity extends OrganisationOwnedEntity {

    @Id
    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Column(name = "street_address", length = 500, nullable = false)
    private String streetAddress;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "province", length = 120)
    private String province;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    public UUID getAddressId() { return addressId; }
    public void setAddressId(UUID addressId) { this.addressId = addressId; }
    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
