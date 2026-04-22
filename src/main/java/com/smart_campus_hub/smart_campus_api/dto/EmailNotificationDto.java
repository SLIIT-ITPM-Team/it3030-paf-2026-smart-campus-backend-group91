package com.smart_campus_hub.smart_campus_api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailNotificationDto {
    private Long id;
    private String recipient;
    private String subject;
    private String body;
    private boolean sent;
    private String failureReason;
    private LocalDateTime createdAt;
}
