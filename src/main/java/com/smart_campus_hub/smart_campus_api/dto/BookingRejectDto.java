package com.smart_campus_hub.smart_campus_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookingRejectDto {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
