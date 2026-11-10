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
import za.ac.mycput.application.service.StaffApplicationService;
import za.ac.mycput.interfaces.rest.dto.ManagerRequest;
import za.ac.mycput.interfaces.rest.dto.ManagerResponse;
import za.ac.mycput.interfaces.rest.mapper.ManagerRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final StaffApplicationService staff;
    private final ManagerRestMapper mapper;

    public ManagerController(StaffApplicationService staff, ManagerRestMapper mapper) {
        this.staff = staff;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ManagerResponse> list() {
        return staff.listManagers().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{managerId}")
    public ManagerResponse get(@PathVariable String managerId) {
        return mapper.toResponse(staff.getManager(managerId));
    }

    @PostMapping
    public ResponseEntity<ManagerResponse> create(@Valid @RequestBody ManagerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(staff.createManager(mapper.toCommand(request))));
    }

    @PutMapping("/{managerId}")
    public ManagerResponse update(@PathVariable String managerId, @Valid @RequestBody ManagerRequest request) {
        return mapper.toResponse(staff.updateManager(managerId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{managerId}")
    public ResponseEntity<Void> delete(@PathVariable String managerId) {
        staff.deleteManager(managerId);
        return ResponseEntity.noContent().build();
    }
}
