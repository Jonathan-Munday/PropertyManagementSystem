/*
 * PropertyController.java
 * REST controller for Property entity.
 * Author: Jonathan Munday (230677797)
 * Date: 19 July 2026
 */
package za.ac.mycput.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.mycput.domain.Property;
import za.ac.mycput.service.IPropertyService;

import java.util.List;

@RestController
@RequestMapping("/property")
public class PropertyController {

    @Autowired
    private IPropertyService propertyService;

    @PostMapping("/create")
    public ResponseEntity<Property> create(@RequestBody Property property) {
        Property created = propertyService.create(property);
        if (created != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Property> read(@PathVariable String id) {
        Property property = propertyService.read(id);
        if (property != null) {
            return ResponseEntity.ok(property);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Property> update(@RequestBody Property property) {
        Property updated = propertyService.update(property);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        boolean deleted = propertyService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Property>> getAll() {
        List<Property> properties = propertyService.getAll();
        return ResponseEntity.ok(properties);
    }
}
