/*
 * TenantController.java
 * REST controller for Tenant entity.
 * Author: Imaad Petersen (230166040)
 * Date: 19 July 2026
 */
package za.ac.mycput.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.mycput.domain.Tenant;
import za.ac.mycput.service.ITenantService;

import java.util.List;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private ITenantService tenantService;

    @PostMapping("/create")
    public ResponseEntity<Tenant> create(@RequestBody Tenant tenant) {
        Tenant created = tenantService.create(tenant);
        if (created != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Tenant> read(@PathVariable String id) {
        Tenant tenant = tenantService.read(id);
        if (tenant != null) {
            return ResponseEntity.ok(tenant);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Tenant> update(@RequestBody Tenant tenant) {
        Tenant updated = tenantService.update(tenant);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        boolean deleted = tenantService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Tenant>> getAll() {
        List<Tenant> tenants = tenantService.getAll();
        return ResponseEntity.ok(tenants);
    }
}
