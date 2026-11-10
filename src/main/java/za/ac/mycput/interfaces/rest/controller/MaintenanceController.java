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
import za.ac.mycput.application.service.MaintenanceApplicationService;
import za.ac.mycput.interfaces.rest.dto.MaintenanceRequest;
import za.ac.mycput.interfaces.rest.dto.MaintenanceResponse;
import za.ac.mycput.interfaces.rest.mapper.MaintenanceRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceApplicationService maintenance;
    private final MaintenanceRestMapper mapper;

    public MaintenanceController(MaintenanceApplicationService maintenance, MaintenanceRestMapper mapper) {
        this.maintenance = maintenance;
        this.mapper = mapper;
    }

    @GetMapping
    public List<MaintenanceResponse> list() {
        return maintenance.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{requestId}")
    public MaintenanceResponse get(@PathVariable String requestId) {
        return mapper.toResponse(maintenance.get(requestId));
    }

    @PostMapping
    public ResponseEntity<MaintenanceResponse> create(@Valid @RequestBody MaintenanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(maintenance.open(mapper.toCommand(request))));
    }

    @PutMapping("/{requestId}")
    public MaintenanceResponse update(@PathVariable String requestId, @Valid @RequestBody MaintenanceRequest request) {
        return mapper.toResponse(maintenance.update(requestId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> delete(@PathVariable String requestId) {
        maintenance.delete(requestId);
        return ResponseEntity.noContent().build();
    }
}
