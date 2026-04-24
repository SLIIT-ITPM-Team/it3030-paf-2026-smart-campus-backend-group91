package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String senderRole;
    private String content;
    private LocalDateTime createdAt;
}
