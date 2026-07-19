/*
 * AgentRoleController.java
 * REST controller for AgentRole entity.
 * Author: Zia Modarrisi (230671942)
 * Date: 19 July 2026
 */
package za.ac.mycput.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.mycput.domain.AgentRole;
import za.ac.mycput.service.IAgentRoleService;

import java.util.List;

@RestController
@RequestMapping("/agentrole")
public class AgentRoleController {

    @Autowired
    private IAgentRoleService agentRoleService;

    @PostMapping("/create")
    public ResponseEntity<AgentRole> create(@RequestBody AgentRole agentRole) {
        AgentRole created = agentRoleService.create(agentRole);
        if (created != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<AgentRole> read(@PathVariable String id) {
        AgentRole agentRole = agentRoleService.read(id);
        if (agentRole != null) {
            return ResponseEntity.ok(agentRole);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update")
    public ResponseEntity<AgentRole> update(@RequestBody AgentRole agentRole) {
        AgentRole updated = agentRoleService.update(agentRole);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        boolean deleted = agentRoleService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<AgentRole>> getAll() {
        List<AgentRole> agentRoles = agentRoleService.getAll();
        return ResponseEntity.ok(agentRoles);
    }
}
