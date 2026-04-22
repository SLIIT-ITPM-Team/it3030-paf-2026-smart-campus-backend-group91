package com.smart_campus_hub.smart_campus_api.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class AvailabilityResponseDto {
    private boolean available;
    private String message;
    private List<AvailableSlotDto> suggestions;

    @Data
    public static class AvailableSlotDto {
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
