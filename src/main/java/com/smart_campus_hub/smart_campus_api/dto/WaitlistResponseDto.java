package com.smart_campus_hub.smart_campus_api.dto;

import com.smart_campus_hub.smart_campus_api.model.WaitlistStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class WaitlistResponseDto {
    private Long waitlistId;
    private Long resourceId;
    private String resourceName;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private WaitlistStatus status;
    private Integer position;
    private LocalDateTime notifiedAt;
    private LocalDateTime claimExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
