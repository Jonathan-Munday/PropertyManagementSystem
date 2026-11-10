package za.ac.mycput.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "vendors")
public class VendorJpaEntity extends OrganisationOwnedEntity {

    @Id
    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "company_name", length = 200, nullable = false)
    private String companyName;

    @Column(name = "contact_number", length = 40, nullable = false)
    private String contactNumber;

    @Column(name = "specialty", length = 120, nullable = false)
    private String specialty;

    public UUID getVendorId() { return vendorId; }
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}
