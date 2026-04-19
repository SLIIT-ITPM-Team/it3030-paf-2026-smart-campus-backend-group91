package com.smart_campus_hub.smart_campus_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateDto {
    @NotBlank(message = "Content cannot be blank")
    private String content;
}
