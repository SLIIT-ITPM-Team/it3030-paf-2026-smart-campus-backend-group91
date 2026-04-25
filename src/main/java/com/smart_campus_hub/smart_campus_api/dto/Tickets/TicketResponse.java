package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import com.smart_campus_hub.smart_campus_api.model.TicketPriority;
import com.smart_campus_hub.smart_campus_api.model.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TicketResponse {
    private Long id;
    private String resourceId;
    private Long createdBy;
    private String createdByName;
    private Long assignedTo;
    private String assignedToName;
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

    // SLA fields
    private Long timeToFirstResponseMinutes;
    private Long timeToResolutionMinutes;
    private Boolean firstResponseSlaMet;
    private Boolean resolutionSlaMet;
    private Long slaFirstResponseMinutes;
    private Long slaResolutionMinutes;

    private List<AttachmentResponse> attachments;
    private List<CommentResponse> comments;
}
