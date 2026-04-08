package com.smart_campus_hub.smart_campus_api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttachmentDto {
    private Long id;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
