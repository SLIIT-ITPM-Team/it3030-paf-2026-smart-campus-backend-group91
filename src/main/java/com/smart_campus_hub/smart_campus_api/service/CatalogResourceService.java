package com.smart_campus_hub.smart_campus_api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smart_campus_hub.smart_campus_api.dto.CatalogAvailabilityWindowDto;
import com.smart_campus_hub.smart_campus_api.dto.CatalogLocationDto;
import com.smart_campus_hub.smart_campus_api.dto.CatalogResourceRequestDto;
import com.smart_campus_hub.smart_campus_api.dto.CatalogResourceResponseDto;
import com.smart_campus_hub.smart_campus_api.entity.AvailabilityWindow;
import com.smart_campus_hub.smart_campus_api.entity.Location;
import com.smart_campus_hub.smart_campus_api.entity.Resource;
import com.smart_campus_hub.smart_campus_api.repository.AvailabilityWindowRepository;
import com.smart_campus_hub.smart_campus_api.repository.LocationRepository;
import com.smart_campus_hub.smart_campus_api.repository.ResourceRepository;

@Service
public class CatalogResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AvailabilityWindowRepository availabilityWindowRepository;

    @Autowired
    private ResourceImageStorageService resourceImageStorageService;

    public List<CatalogResourceResponseDto> getResources(
            String keyword,
            String type,
            Integer minCapacity,
            String status,
            String locationKeyword) {
        String search = normalize(keyword);
        String locationSearch = normalize(locationKeyword);

        return resourceRepository.findAll()
                .stream()
                .filter(resource -> matchesType(resource, type))
                .filter(resource -> matchesStatus(resource, status))
                .filter(resource -> matchesCapacity(resource, minCapacity))
                .filter(resource -> matchesKeyword(resource, search))
                .filter(resource -> matchesLocation(resource, locationSearch))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CatalogResourceResponseDto getResource(Long id) {
        Resource resource = findResource(id);
        return mapToDto(resource);
    }

    public CatalogResourceResponseDto createResource(CatalogResourceRequestDto dto) {
        Resource resource = new Resource();
        applyCoreFields(resource, dto);
        Location location = resolveLocation(dto.getLocation(), null);
        resource.setLocation(location);
        Resource saved = resourceRepository.save(resource);
        replaceAvailabilityWindows(saved, dto.getAvailabilityWindows());
        return mapToDto(saved);
    }

    public CatalogResourceResponseDto updateResource(Long id, CatalogResourceRequestDto dto) {
        Resource resource = findResource(id);
        applyCoreFields(resource, dto);
        if (dto.getLocation() != null) {
            Location location = resolveLocation(dto.getLocation(), resource.getLocation());
            resource.setLocation(location);
        }
        Resource saved = resourceRepository.save(resource);
        replaceAvailabilityWindows(saved, dto.getAvailabilityWindows());
        return mapToDto(saved);
    }

    public CatalogResourceResponseDto updateStatus(Long id, String status) {
        Resource resource = findResource(id);
        resource.setStatus(status);
        return mapToDto(resourceRepository.save(resource));
    }

    public CatalogResourceResponseDto updateImage(Long id, MultipartFile file) {
        Resource resource = findResource(id);
        try {
            resource.setImageData(file.getBytes());
            resource.setImageContentType(file.getContentType());
            resource.setImageUrl(null);
        } catch (Exception ex) {
            throw new RuntimeException("Could not store image", ex);
        }
        return mapToDto(resourceRepository.save(resource));
    }

    public String getImageUrl(Long id) {
        Resource resource = findResource(id);
        return resource.getImageUrl();
    }

    public void deleteResource(Long id) {
        Resource resource = findResource(id);
        List<AvailabilityWindow> windows = availabilityWindowRepository.findByResource_Id(resource.getId());
        availabilityWindowRepository.deleteAll(windows);
        resourceRepository.delete(resource);
    }

    public List<CatalogAvailabilityWindowDto> replaceAvailability(Long resourceId, List<CatalogAvailabilityWindowDto> windows) {
        Resource resource = findResource(resourceId);
        replaceAvailabilityWindows(resource, windows);
        return mapAvailabilityWindows(availabilityWindowRepository.findByResource_Id(resourceId));
    }

    private Resource findResource(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    private void applyCoreFields(Resource resource, CatalogResourceRequestDto dto) {
        if (dto.getName() != null) {
            resource.setName(dto.getName().trim());
        }
        if (dto.getType() != null) {
            resource.setType(dto.getType().trim());
        }
        if (dto.getCapacity() != null) {
            resource.setCapacity(dto.getCapacity());
        }
        if (dto.getStatus() != null) {
            resource.setStatus(dto.getStatus().trim());
        }
        if (dto.getImageUrl() != null) {
            resource.setImageUrl(dto.getImageUrl().trim());
        }
    }

    private Location resolveLocation(CatalogLocationDto dto, Location fallback) {
        if (dto == null) {
            return fallback;
        }

        Location location = null;
        if (dto.getId() != null) {
            location = locationRepository.findById(dto.getId()).orElse(null);
        }

        if (location == null) {
            location = fallback != null ? fallback : new Location();
        }

        if (dto.getBuildingName() != null) {
            location.setBuildingName(dto.getBuildingName().trim());
        }
        if (dto.getFloorNumber() != null) {
            location.setFloorNumber(dto.getFloorNumber().trim());
        }
        if (dto.getRoomNumber() != null) {
            location.setRoomNumber(dto.getRoomNumber().trim());
        }
        if (dto.getDescription() != null) {
            location.setDescription(dto.getDescription().trim());
        }

        return locationRepository.save(location);
    }

    private void replaceAvailabilityWindows(Resource resource, List<CatalogAvailabilityWindowDto> windows) {
        if (windows == null) {
            return;
        }

        List<AvailabilityWindow> existing = availabilityWindowRepository.findByResource_Id(resource.getId());
        availabilityWindowRepository.deleteAll(existing);

        List<AvailabilityWindow> newWindows = new ArrayList<>();
        for (CatalogAvailabilityWindowDto dto : windows) {
            if (dto == null) {
                continue;
            }
            AvailabilityWindow window = new AvailabilityWindow();
            window.setDayOfWeek(dto.getDayOfWeek());
            window.setStartTime(dto.getStartTime());
            window.setEndTime(dto.getEndTime());
            window.setResource(resource);
            newWindows.add(window);
        }

        availabilityWindowRepository.saveAll(newWindows);
    }

    private boolean matchesType(Resource resource, String type) {
        if (type == null || type.isBlank()) {
            return true;
        }
        return resource.getType() != null
                && resource.getType().equalsIgnoreCase(type.trim());
    }

    private boolean matchesStatus(Resource resource, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return resource.getStatus() != null
                && resource.getStatus().equalsIgnoreCase(status.trim());
    }

    private boolean matchesCapacity(Resource resource, Integer minCapacity) {
        if (minCapacity == null) {
            return true;
        }
        return resource.getCapacity() != null
                && resource.getCapacity() >= minCapacity;
    }

    private boolean matchesKeyword(Resource resource, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        String source = String.format(Locale.ROOT, "%s %s %s",
                safe(resource.getName()), safe(resource.getType()), safe(resource.getStatus()));
        return source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean matchesLocation(Resource resource, String locationKeyword) {
        if (locationKeyword.isBlank()) {
            return true;
        }
        Location location = resource.getLocation();
        if (location == null) {
            return false;
        }
        String source = String.format(Locale.ROOT, "%s %s %s %s",
                safe(location.getBuildingName()),
                safe(location.getFloorNumber()),
                safe(location.getRoomNumber()),
                safe(location.getDescription()));
        return source.toLowerCase(Locale.ROOT).contains(locationKeyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private CatalogResourceResponseDto mapToDto(Resource resource) {
        CatalogResourceResponseDto dto = new CatalogResourceResponseDto();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setType(resource.getType());
        dto.setCapacity(resource.getCapacity());
        dto.setStatus(resource.getStatus());
        dto.setImageUrl(resource.getImageUrl());
        dto.setLocation(mapLocation(resource.getLocation()));
        dto.setAvailabilityWindows(mapAvailabilityWindows(
                availabilityWindowRepository.findByResource_Id(resource.getId())));
        return dto;
    }

    private CatalogLocationDto mapLocation(Location location) {
        if (location == null) {
            return null;
        }
        CatalogLocationDto dto = new CatalogLocationDto();
        dto.setId(location.getId());
        dto.setBuildingName(location.getBuildingName());
        dto.setFloorNumber(location.getFloorNumber());
        dto.setRoomNumber(location.getRoomNumber());
        dto.setDescription(location.getDescription());
        return dto;
    }

    private List<CatalogAvailabilityWindowDto> mapAvailabilityWindows(List<AvailabilityWindow> windows) {
        return windows.stream().filter(Objects::nonNull).map(window -> {
            CatalogAvailabilityWindowDto dto = new CatalogAvailabilityWindowDto();
            dto.setId(window.getId());
            dto.setDayOfWeek(window.getDayOfWeek());
            dto.setStartTime(window.getStartTime());
            dto.setEndTime(window.getEndTime());
            return dto;
        }).collect(Collectors.toList());
    }
}
