/*
 * LeaseController.java
 * REST controller for Lease entity.
 * Author: Malik Muhammed (230388175)
 * Date: 19 July 2026
 */
package za.ac.mycput.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.mycput.domain.Lease;
import za.ac.mycput.service.ILeaseService;

import java.util.List;

@RestController
@RequestMapping("/lease")
public class LeaseController {

    @Autowired
    private ILeaseService leaseService;

    @PostMapping("/create")
    public ResponseEntity<Lease> create(@RequestBody Lease lease) {
        Lease created = leaseService.create(lease);
        if (created != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Lease> read(@PathVariable String id) {
        Lease lease = leaseService.read(id);
        if (lease != null) {
            return ResponseEntity.ok(lease);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Lease> update(@RequestBody Lease lease) {
        Lease updated = leaseService.update(lease);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        boolean deleted = leaseService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Lease>> getAll() {
        List<Lease> leases = leaseService.getAll();
        return ResponseEntity.ok(leases);
    }
}
