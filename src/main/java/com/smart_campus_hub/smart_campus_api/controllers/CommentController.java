package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.CommentCreateDto;
import com.smart_campus_hub.smart_campus_api.dto.CommentResponseDto;
import com.smart_campus_hub.smart_campus_api.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/tickets/{id}/comments")
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable("id") Long ticketId,
            @Valid @RequestBody CommentCreateDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(commentService.addComment(ticketId, dto, userId, userRole));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponseDto> editComment(
            @PathVariable("id") Long commentId,
            @Valid @RequestBody CommentCreateDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(commentService.editComment(commentId, dto, userId));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("id") Long commentId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
