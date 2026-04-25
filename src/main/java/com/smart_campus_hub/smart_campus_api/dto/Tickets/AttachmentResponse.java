package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentResponse {
    private Long id;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
