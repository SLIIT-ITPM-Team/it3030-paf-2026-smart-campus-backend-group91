package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketAssignRequest {
    @NotNull(message = "Technician ID must be provided")
    private Long assignedToId;
}
