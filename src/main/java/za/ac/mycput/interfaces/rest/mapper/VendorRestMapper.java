package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.VendorCommand;
import za.ac.mycput.domain.model.Vendor;
import za.ac.mycput.interfaces.rest.dto.VendorRequest;
import za.ac.mycput.interfaces.rest.dto.VendorResponse;

@Component
public class VendorRestMapper {

    public VendorCommand toCommand(VendorRequest request) {
        return new VendorCommand(
                request.vendorId(),
                request.companyName(),
                request.contactNumber(),
                request.specialty());
    }

    public VendorResponse toResponse(Vendor vendor) {
        return new VendorResponse(
                vendor.getVendorId(),
                vendor.getCompanyName(),
                vendor.getContactNumber(),
                vendor.getSpecialty());
    }
}
