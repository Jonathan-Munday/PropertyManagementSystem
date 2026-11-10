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
import za.ac.mycput.application.service.TenantApplicationService;
import za.ac.mycput.interfaces.rest.dto.TenantRequest;
import za.ac.mycput.interfaces.rest.dto.TenantResponse;
import za.ac.mycput.interfaces.rest.mapper.TenantRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    private final TenantApplicationService tenants;
    private final TenantRestMapper mapper;

    public TenantController(TenantApplicationService tenants, TenantRestMapper mapper) {
        this.tenants = tenants;
        this.mapper = mapper;
    }

    @GetMapping
    public List<TenantResponse> list() {
        return tenants.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{tenantId}")
    public TenantResponse get(@PathVariable String tenantId) {
        return mapper.toResponse(tenants.get(tenantId));
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create(@Valid @RequestBody TenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(tenants.create(mapper.toCommand(request))));
    }

    @PutMapping("/{tenantId}")
    public TenantResponse update(@PathVariable String tenantId, @Valid @RequestBody TenantRequest request) {
        return mapper.toResponse(tenants.update(tenantId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> delete(@PathVariable String tenantId) {
        tenants.delete(tenantId);
        return ResponseEntity.noContent().build();
    }
}
