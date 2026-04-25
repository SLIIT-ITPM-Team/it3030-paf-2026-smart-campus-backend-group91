package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRejectRequest {
    @NotBlank(message = "Rejection reason must be provided")
    private String reason;
}
