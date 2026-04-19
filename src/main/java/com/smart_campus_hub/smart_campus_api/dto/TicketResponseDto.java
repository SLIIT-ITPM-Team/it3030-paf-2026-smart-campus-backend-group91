package com.smart_campus_hub.smart_campus_api.dto;

import com.smart_campus_hub.smart_campus_api.model.TicketPriority;
import com.smart_campus_hub.smart_campus_api.model.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketResponseDto {
    private Long id;
    private String resourceId;
    private Long createdBy;
    private Long assignedTo;
    private String category;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private String contactDetails;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;

    private Long responseTimeMinutes;
    private Long resolutionTimeMinutes;

    private List<CommentResponseDto> comments;
    private List<AttachmentDto> attachments;
}
