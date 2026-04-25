package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import com.smart_campus_hub.smart_campus_api.model.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCreateRequest {
    private String resourceId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotBlank(message = "Contact details are required")
    private String contactDetails;
}
