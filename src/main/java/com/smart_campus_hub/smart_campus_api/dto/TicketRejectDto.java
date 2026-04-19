package com.smart_campus_hub.smart_campus_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRejectDto {
    @NotBlank(message = "Reason is required for rejection")
    private String reason;
}
