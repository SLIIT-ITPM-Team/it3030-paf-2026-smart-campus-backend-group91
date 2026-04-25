package com.smart_campus_hub.smart_campus_api.dto;

import lombok.Data;

@Data
public class CatalogLocationDto {
    private Long id;
    private String buildingName;
    private String floorNumber;
    private String roomNumber;
    private String description;
}
