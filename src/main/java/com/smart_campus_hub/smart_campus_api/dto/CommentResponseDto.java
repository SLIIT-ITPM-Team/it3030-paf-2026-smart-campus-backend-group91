package com.smart_campus_hub.smart_campus_api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
