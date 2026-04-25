package com.smart_campus_hub.smart_campus_api.controllers;

import java.util.List;

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

import com.smart_campus_hub.smart_campus_api.dto.ResourceResponseDto;
import com.smart_campus_hub.smart_campus_api.entity.Resource;
import com.smart_campus_hub.smart_campus_api.model.ResourceStatus;
import com.smart_campus_hub.smart_campus_api.model.ResourceType;
import com.smart_campus_hub.smart_campus_api.repository.ResourceRepository;
import com.smart_campus_hub.smart_campus_api.service.ResourceService;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin("*")
public class ResourceController {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceService resourceService;

    // 1. GET ALL & ADVANCED FILTERING (Type, Capacity, Status)
        @GetMapping
        public List<ResourceResponseDto> getAllResources(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status) {
        return resourceService.getResources(keyword, type, status);
        }

        @GetMapping("/{id}")
        public ResourceResponseDto getResource(@PathVariable Long id) {
        return resourceService.getResource(id);
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
