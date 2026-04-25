package com.smart_campus_hub.smart_campus_api.dto;

import java.util.List;

import lombok.Data;

@Data
public class CatalogResourceRequestDto {
    private String name;
    private String type;
    private Integer capacity;
    private String status;
    private CatalogLocationDto location;
    private List<CatalogAvailabilityWindowDto> availabilityWindows;
}
