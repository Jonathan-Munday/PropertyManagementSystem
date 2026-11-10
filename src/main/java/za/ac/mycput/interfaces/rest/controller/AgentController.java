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
import za.ac.mycput.interfaces.rest.dto.AgentRequest;
import za.ac.mycput.interfaces.rest.dto.AgentResponse;
import za.ac.mycput.interfaces.rest.mapper.AgentRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final StaffApplicationService staff;
    private final AgentRestMapper mapper;

    public AgentController(StaffApplicationService staff, AgentRestMapper mapper) {
        this.staff = staff;
        this.mapper = mapper;
    }

    @GetMapping
    public List<AgentResponse> list() {
        return staff.listAgents().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{agentId}")
    public AgentResponse get(@PathVariable String agentId) {
        return mapper.toResponse(staff.getAgent(agentId));
    }

    @PostMapping
    public ResponseEntity<AgentResponse> create(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(staff.createAgent(mapper.toCommand(request))));
    }

    @PutMapping("/{agentId}")
    public AgentResponse update(@PathVariable String agentId, @Valid @RequestBody AgentRequest request) {
        return mapper.toResponse(staff.updateAgent(agentId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{agentId}")
    public ResponseEntity<Void> delete(@PathVariable String agentId) {
        staff.deleteAgent(agentId);
        return ResponseEntity.noContent().build();
    }
}
