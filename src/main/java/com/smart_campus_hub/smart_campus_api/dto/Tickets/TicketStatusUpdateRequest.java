package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import com.smart_campus_hub.smart_campus_api.model.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketStatusUpdateRequest {
    @NotNull(message = "Status cannot be null")
    private TicketStatus status;

    private String resolutionNotes;
}
