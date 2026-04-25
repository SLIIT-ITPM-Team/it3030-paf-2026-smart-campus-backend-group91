package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketResolveRequest {
    @NotBlank(message = "Resolution notes are required when resolving a ticket")
    private String resolutionNotes;
}
