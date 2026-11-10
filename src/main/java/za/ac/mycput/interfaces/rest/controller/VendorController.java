package za.ac.mycput.interfaces.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.ac.mycput.application.service.DirectoryApplicationService;
import za.ac.mycput.interfaces.rest.dto.VendorRequest;
import za.ac.mycput.interfaces.rest.dto.VendorResponse;
import za.ac.mycput.interfaces.rest.mapper.VendorRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {

    private final DirectoryApplicationService directory;
    private final VendorRestMapper mapper;

    public VendorController(DirectoryApplicationService directory, VendorRestMapper mapper) {
        this.directory = directory;
        this.mapper = mapper;
    }

    @GetMapping
    public List<VendorResponse> list() {
        return directory.listVendors().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{vendorId}")
    public VendorResponse get(@PathVariable String vendorId) {
        return mapper.toResponse(directory.getVendor(vendorId));
    }

    @PostMapping
    public ResponseEntity<VendorResponse> create(@Valid @RequestBody VendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(directory.createVendor(mapper.toCommand(request))));
    }

    @PutMapping("/{vendorId}")
    public VendorResponse update(@PathVariable String vendorId, @Valid @RequestBody VendorRequest request) {
        return mapper.toResponse(directory.updateVendor(vendorId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{vendorId}")
    public ResponseEntity<Void> delete(@PathVariable String vendorId) {
        directory.deleteVendor(vendorId);
        return ResponseEntity.noContent().build();
    }
}
