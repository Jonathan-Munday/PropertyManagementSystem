/*
 * ManagerController.java
 * REST controller for Manager entity.
 * Author: Anas Pansari (230195342)
 * Date: 19 July 2026
 */
package za.ac.mycput.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.mycput.domain.Manager;
import za.ac.mycput.service.IManagerService;

import java.util.List;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private IManagerService managerService;

    @PostMapping("/create")
    public ResponseEntity<Manager> create(@RequestBody Manager manager) {
        Manager created = managerService.create(manager);
        if (created != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Manager> read(@PathVariable String id) {
        Manager manager = managerService.read(id);
        if (manager != null) {
            return ResponseEntity.ok(manager);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Manager> update(@RequestBody Manager manager) {
        Manager updated = managerService.update(manager);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        boolean deleted = managerService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Manager>> getAll() {
        List<Manager> managers = managerService.getAll();
        return ResponseEntity.ok(managers);
    }
}
