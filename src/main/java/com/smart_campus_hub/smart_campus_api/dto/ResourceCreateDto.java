package com.smart_campus_hub.smart_campus_api.dto;

import com.smart_campus_hub.smart_campus_api.model.ResourceStatus;
import com.smart_campus_hub.smart_campus_api.model.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceCreateDto {
    @NotBlank(message = "Resource name is required")
    private String name;

    @NotNull(message = "Resource type is required")
    private ResourceType type;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 0, message = "Capacity cannot be negative")
    private Integer capacity;

    @NotNull(message = "Resource status is required")
    private ResourceStatus status;

    private String description;
}
