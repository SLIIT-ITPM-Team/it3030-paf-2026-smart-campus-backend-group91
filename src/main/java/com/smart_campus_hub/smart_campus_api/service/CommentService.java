package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.CommentCreateDto;
import com.smart_campus_hub.smart_campus_api.dto.CommentResponseDto;
import com.smart_campus_hub.smart_campus_api.model.Comment;
import com.smart_campus_hub.smart_campus_api.model.Ticket;
import com.smart_campus_hub.smart_campus_api.model.TicketStatus;
import com.smart_campus_hub.smart_campus_api.repository.CommentRepository;
import com.smart_campus_hub.smart_campus_api.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Transactional
    public CommentResponseDto addComment(Long ticketId, CommentCreateDto dto, Long userId, String userRole) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        Comment comment = Comment.builder()
                .ticket(ticket)
                .userId(userId)
                .content(dto.getContent())
                .build();

        comment = commentRepository.save(comment);

        // Timer Logic: set firstResponseAt if technician comments and it's not set
        if ("TECHNICIAN".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole)) {
            if (ticket.getFirstResponseAt() == null) {
                ticket.setFirstResponseAt(LocalDateTime.now());
                ticketRepository.save(ticket);
            }
        }

        return mapToDto(comment);
    }

    @Transactional
    public CommentResponseDto editComment(Long commentId, CommentCreateDto dto, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only edit your own comments");
        }

        comment.setContent(dto.getContent());
        comment = commentRepository.save(comment);
        return mapToDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponseDto mapToDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
