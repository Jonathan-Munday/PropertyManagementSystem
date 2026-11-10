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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.ac.mycput.application.service.PropertyApplicationService;
import za.ac.mycput.interfaces.rest.dto.PropertyRequest;
import za.ac.mycput.interfaces.rest.dto.PropertyResponse;
import za.ac.mycput.interfaces.rest.mapper.PropertyRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/property")
public class PropertyController {

    private final PropertyApplicationService properties;
    private final PropertyRestMapper mapper;

    public PropertyController(PropertyApplicationService properties, PropertyRestMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
    }

    @GetMapping
    public List<PropertyResponse> list(@RequestParam(name = "available", required = false) Boolean available) {
        return properties.list(available).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{propertyId}")
    public PropertyResponse get(@PathVariable String propertyId) {
        return mapper.toResponse(properties.get(propertyId));
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(properties.create(mapper.toCommand(request))));
    }

    @PutMapping("/{propertyId}")
    public PropertyResponse update(@PathVariable String propertyId, @Valid @RequestBody PropertyRequest request) {
        return mapper.toResponse(properties.update(propertyId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> delete(@PathVariable String propertyId) {
        properties.delete(propertyId);
        return ResponseEntity.noContent().build();
    }
}
