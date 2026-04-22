package com.smart_campus_hub.smart_campus_api.dto;

import com.smart_campus_hub.smart_campus_api.model.ResourceStatus;
import com.smart_campus_hub.smart_campus_api.model.ResourceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceResponseDto {
    private Long id;
    private String name;
    private ResourceType type;
    private String location;
    private Integer capacity;
    private ResourceStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
