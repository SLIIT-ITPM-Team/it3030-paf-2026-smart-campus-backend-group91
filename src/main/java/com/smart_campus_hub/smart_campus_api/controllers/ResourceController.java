package com.smart_campus_hub.smart_campus_api.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smart_campus_hub.smart_campus_api.entity.Resource;
import com.smart_campus_hub.smart_campus_api.repository.ResourceRepository;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin("*")
public class ResourceController {

    @Autowired
    private ResourceRepository resourceRepository;

    // 1. GET ALL & ADVANCED FILTERING (Type, Capacity, Status)
    @GetMapping
    public List<Resource> getAllResources(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String status) {

        List<Resource> resources = resourceRepository.findAll();

        // Advanced Filtering Logic
        if (type != null && !type.isEmpty()) {
            resources = resources.stream()
                    .filter(resource -> resource.getType() != null
                            && resource.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        if (minCapacity != null) {
            resources = resources.stream()
                    .filter(resource -> resource.getCapacity() != null
                            && resource.getCapacity() >= minCapacity)
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            resources = resources.stream()
                    .filter(resource -> resource.getStatus() != null
                            && resource.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        return resources;
    }

    // 2. CREATE NEW RESOURCE
    @PostMapping
    public Resource createResource(@RequestBody Resource resource) {
        // Default status is ACTIVE when creating a new resource
        if (resource.getStatus() == null) {
            resource.setStatus("ACTIVE");
        }
        return resourceRepository.save(resource);
    }

    // 3. UPDATE EXISTING RESOURCE
    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(@PathVariable Long id, @RequestBody Resource resourceDetails) {
        return resourceRepository.findById(id).map(resource -> {
            resource.setName(resourceDetails.getName());
            resource.setType(resourceDetails.getType());
            resource.setCapacity(resourceDetails.getCapacity());
            resource.setStatus(resourceDetails.getStatus());
            resource.setLocation(resourceDetails.getLocation());
            Resource updatedResource = resourceRepository.save(resource);
            return ResponseEntity.ok(updatedResource);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE A RESOURCE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        if (resourceRepository.existsById(id)) {
            resourceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 5. UPDATE RESOURCE STATUS (e.g., ACTIVE to OUT_OF_SERVICE)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Resource> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return resourceRepository.findById(id).map(resource -> {
            resource.setStatus(status);
            Resource updatedResource = resourceRepository.save(resource);
            return ResponseEntity.ok(updatedResource);
        }).orElse(ResponseEntity.notFound().build());
    }
}
