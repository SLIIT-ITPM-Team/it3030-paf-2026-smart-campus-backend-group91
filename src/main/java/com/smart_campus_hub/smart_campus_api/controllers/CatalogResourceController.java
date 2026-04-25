package com.smart_campus_hub.smart_campus_api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smart_campus_hub.smart_campus_api.dto.CatalogAvailabilityWindowDto;
import com.smart_campus_hub.smart_campus_api.dto.CatalogResourceRequestDto;
import com.smart_campus_hub.smart_campus_api.dto.CatalogResourceResponseDto;
import com.smart_campus_hub.smart_campus_api.entity.Resource;
import com.smart_campus_hub.smart_campus_api.repository.ResourceRepository;
import com.smart_campus_hub.smart_campus_api.service.CatalogResourceService;

@RestController
@RequestMapping("/api/catalog/resources")
@CrossOrigin("*")
public class CatalogResourceController {

    @Autowired
    private CatalogResourceService catalogResourceService;

    @Autowired
    private ResourceRepository resourceRepository;

    @GetMapping
    public List<CatalogResourceResponseDto> getResources(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location) {
        return catalogResourceService.getResources(keyword, type, minCapacity, status, location);
    }

    @GetMapping("/{id}")
    public CatalogResourceResponseDto getResource(@PathVariable Long id) {
        return catalogResourceService.getResource(id);
    }

    @PostMapping
    public CatalogResourceResponseDto createResource(@RequestBody CatalogResourceRequestDto dto) {
        return catalogResourceService.createResource(dto);
    }

    @PutMapping("/{id}")
    public CatalogResourceResponseDto updateResource(@PathVariable Long id, @RequestBody CatalogResourceRequestDto dto) {
        return catalogResourceService.updateResource(id, dto);
    }

    @PatchMapping("/{id}/status")
    public CatalogResourceResponseDto updateStatus(@PathVariable Long id, @RequestParam String status) {
        return catalogResourceService.updateStatus(id, status);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CatalogResourceResponseDto uploadImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        return catalogResourceService.updateImage(id, file);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Resource resource = resourceRepository.findById(id).orElse(null);
        if (resource == null || resource.getImageData() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String contentType = resource.getImageContentType() != null
                ? resource.getImageContentType()
                : "image/jpeg";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        return new ResponseEntity<>(resource.getImageData(), headers, HttpStatus.OK);
    }

    @PutMapping("/{id}/availability")
    public List<CatalogAvailabilityWindowDto> replaceAvailability(
            @PathVariable Long id,
            @RequestBody List<CatalogAvailabilityWindowDto> windows) {
        return catalogResourceService.replaceAvailability(id, windows);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        catalogResourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
