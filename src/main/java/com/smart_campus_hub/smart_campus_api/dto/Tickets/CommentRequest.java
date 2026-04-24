package com.smart_campus_hub.smart_campus_api.dto.Tickets;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "Comment content cannot be empty")
    private String content;

    private String senderRole = "USER";
}
