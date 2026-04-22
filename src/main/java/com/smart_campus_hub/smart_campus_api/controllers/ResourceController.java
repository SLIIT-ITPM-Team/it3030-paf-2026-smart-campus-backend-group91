package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.ResourceCreateDto;
import com.smart_campus_hub.smart_campus_api.dto.ResourceResponseDto;
import com.smart_campus_hub.smart_campus_api.model.ResourceStatus;
import com.smart_campus_hub.smart_campus_api.model.ResourceType;
import com.smart_campus_hub.smart_campus_api.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping
    public ResponseEntity<List<ResourceResponseDto>> getResources(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status) {
        return ResponseEntity.ok(resourceService.getResources(keyword, type, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponseDto> getResource(@PathVariable("id") Long id) {
        return ResponseEntity.ok(resourceService.getResource(id));
    }

    @PostMapping
    public ResponseEntity<ResourceResponseDto> createResource(
            @Valid @RequestBody ResourceCreateDto dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(resourceService.createResource(dto, userRole));
    }
}
