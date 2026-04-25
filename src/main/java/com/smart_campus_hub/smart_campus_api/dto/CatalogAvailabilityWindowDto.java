package com.smart_campus_hub.smart_campus_api.dto;

import java.time.LocalTime;

import lombok.Data;

@Data
public class CatalogAvailabilityWindowDto {
    private Long id;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
