package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.Tickets.CommentRequest;
import com.smart_campus_hub.smart_campus_api.dto.Tickets.CommentResponse;
import com.smart_campus_hub.smart_campus_api.exception.ResourceNotFoundException;
import com.smart_campus_hub.smart_campus_api.exception.UnauthorizedException;
import com.smart_campus_hub.smart_campus_api.model.Comment;
import com.smart_campus_hub.smart_campus_api.model.Ticket;
import com.smart_campus_hub.smart_campus_api.repository.CommentRepository;
import com.smart_campus_hub.smart_campus_api.repository.TicketRepository;
import com.smart_campus_hub.smart_campus_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CommentResponse addComment(Long ticketId, CommentRequest req, Long userId, String userRole) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        String userName = userRepository.findById(userId)
                .map(u -> u.getUsername())
                .orElse("Unknown");

        String senderRole = (req.getSenderRole() != null) ? req.getSenderRole() : userRole;

        Comment comment = Comment.builder()
                .ticket(ticket)
                .userId(userId)
                .userName(userName)
                .senderRole(senderRole)
                .content(req.getContent())
                .build();

        comment = commentRepository.save(comment);

        if ("TECHNICIAN".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole)) {
            if (ticket.getFirstResponseAt() == null) {
                ticket.setFirstResponseAt(LocalDateTime.now());
                ticketRepository.save(ticket);
            }
        }

        return toResponse(comment);
    }

    @Transactional
    public CommentResponse editComment(Long commentId, CommentRequest req, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own comments.");
        }

        comment.setContent(req.getContent());
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own comments.");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .userName(c.getUserName())
                .senderRole(c.getSenderRole())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
