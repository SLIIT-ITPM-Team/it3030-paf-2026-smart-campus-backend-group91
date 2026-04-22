package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.ResourceCreateDto;
import com.smart_campus_hub.smart_campus_api.dto.ResourceResponseDto;
import com.smart_campus_hub.smart_campus_api.model.CampusResource;
import com.smart_campus_hub.smart_campus_api.model.ResourceStatus;
import com.smart_campus_hub.smart_campus_api.model.ResourceType;
import com.smart_campus_hub.smart_campus_api.repository.CampusResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private CampusResourceRepository campusResourceRepository;

    public List<ResourceResponseDto> getResources(String keyword, ResourceType type, ResourceStatus status) {
        String search = keyword == null ? "" : keyword.trim().toLowerCase();
        return campusResourceRepository.findAll()
                .stream()
                .filter(resource -> type == null || resource.getType() == type)
                .filter(resource -> status == null || resource.getStatus() == status)
                .filter(resource -> search.isEmpty() || matches(resource, search))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ResourceResponseDto getResource(Long id) {
        return mapToDto(findResource(id));
    }

    public ResourceResponseDto createResource(ResourceCreateDto dto, String userRole) {
        requireAdmin(userRole);
        CampusResource resource = CampusResource.builder()
                .name(dto.getName())
                .type(dto.getType())
                .location(dto.getLocation())
                .capacity(dto.getCapacity())
                .status(dto.getStatus())
                .description(dto.getDescription())
                .build();
        return mapToDto(campusResourceRepository.save(resource));
    }

    public CampusResource findResource(Long id) {
        return campusResourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    private boolean matches(CampusResource resource, String search) {
        String source = String.format(
                "%s %s %s %s",
                resource.getName(),
                resource.getType(),
                resource.getLocation(),
                resource.getCapacity());
        return source.toLowerCase().contains(search);
    }

    private void requireAdmin(String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can manage resources.");
        }
    }

    private ResourceResponseDto mapToDto(CampusResource resource) {
        ResourceResponseDto dto = new ResourceResponseDto();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setType(resource.getType());
        dto.setLocation(resource.getLocation());
        dto.setCapacity(resource.getCapacity());
        dto.setStatus(resource.getStatus());
        dto.setDescription(resource.getDescription());
        dto.setCreatedAt(resource.getCreatedAt());
        dto.setUpdatedAt(resource.getUpdatedAt());
        return dto;
    }
}
