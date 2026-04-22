package com.smart_campus_hub.smart_campus_api.dto;

import com.smart_campus_hub.smart_campus_api.model.BookingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingResponseDto {
    private Long bookingId;
    private Long resourceId;
    private String resourceName;
    private String resourceLocation;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private Integer expectedAttendees;
    private BookingStatus status;
    private String rejectionReason;
    private String cancellationReason;
    private String qrCodeValue;
    private LocalDateTime qrGeneratedAt;
    private boolean reminderSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
