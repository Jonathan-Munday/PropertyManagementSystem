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
import za.ac.mycput.application.service.LeaseApplicationService;
import za.ac.mycput.interfaces.rest.dto.LeaseRequest;
import za.ac.mycput.interfaces.rest.dto.LeaseResponse;
import za.ac.mycput.interfaces.rest.mapper.LeaseRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/lease")
public class LeaseController {

    private final LeaseApplicationService leases;
    private final LeaseRestMapper mapper;

    public LeaseController(LeaseApplicationService leases, LeaseRestMapper mapper) {
        this.leases = leases;
        this.mapper = mapper;
    }

    @GetMapping
    public List<LeaseResponse> list() {
        return leases.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{leaseId}")
    public LeaseResponse get(@PathVariable String leaseId) {
        return mapper.toResponse(leases.get(leaseId));
    }

    @PostMapping
    public ResponseEntity<LeaseResponse> create(@Valid @RequestBody LeaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(leases.create(mapper.toCommand(request))));
    }

    @PutMapping("/{leaseId}")
    public LeaseResponse update(@PathVariable String leaseId, @Valid @RequestBody LeaseRequest request) {
        return mapper.toResponse(leases.update(leaseId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{leaseId}")
    public ResponseEntity<Void> delete(@PathVariable String leaseId) {
        leases.delete(leaseId);
        return ResponseEntity.noContent().build();
    }
}
