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
import za.ac.mycput.interfaces.rest.dto.AddressRequest;
import za.ac.mycput.interfaces.rest.dto.AddressResponse;
import za.ac.mycput.interfaces.rest.mapper.AddressRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final DirectoryApplicationService directory;
    private final AddressRestMapper mapper;

    public AddressController(DirectoryApplicationService directory, AddressRestMapper mapper) {
        this.directory = directory;
        this.mapper = mapper;
    }

    @GetMapping
    public List<AddressResponse> list() {
        return directory.listAddresses().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{addressId}")
    public AddressResponse get(@PathVariable String addressId) {
        return mapper.toResponse(directory.getAddress(addressId));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(directory.createAddress(mapper.toCommand(request))));
    }

    @PutMapping("/{addressId}")
    public AddressResponse update(@PathVariable String addressId, @Valid @RequestBody AddressRequest request) {
        return mapper.toResponse(directory.updateAddress(addressId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable String addressId) {
        directory.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }
}
