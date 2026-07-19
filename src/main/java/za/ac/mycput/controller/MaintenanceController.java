/*
 * MaintenanceController.java
 * REST controller for Maintenance entity.
 * Author: Olwethu Mtwazi (230036937)
 * Date: 19 July 2026
 */
package za.ac.mycput.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.mycput.domain.Maintenance;
import za.ac.mycput.service.IMaintenanceService;

import java.util.List;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private IMaintenanceService maintenanceService;

    @PostMapping("/create")
    public ResponseEntity<Maintenance> create(@RequestBody Maintenance maintenance) {
        Maintenance created = maintenanceService.create(maintenance);
        if (created != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Maintenance> read(@PathVariable String id) {
        Maintenance maintenance = maintenanceService.read(id);
        if (maintenance != null) {
            return ResponseEntity.ok(maintenance);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Maintenance> update(@RequestBody Maintenance maintenance) {
        Maintenance updated = maintenanceService.update(maintenance);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        boolean deleted = maintenanceService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Maintenance>> getAll() {
        List<Maintenance> maintenances = maintenanceService.getAll();
        return ResponseEntity.ok(maintenances);
    }
}
