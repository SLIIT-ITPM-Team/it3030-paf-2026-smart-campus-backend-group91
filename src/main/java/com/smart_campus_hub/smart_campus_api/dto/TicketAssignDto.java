package com.smart_campus_hub.smart_campus_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketAssignDto {
    @NotNull(message = "Assignee cannot be null")
    private Long assignTo;
}
