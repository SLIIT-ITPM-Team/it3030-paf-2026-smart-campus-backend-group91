package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.*;
import com.smart_campus_hub.smart_campus_api.model.Attachment;
import com.smart_campus_hub.smart_campus_api.model.Comment;
import com.smart_campus_hub.smart_campus_api.model.Ticket;
import com.smart_campus_hub.smart_campus_api.model.TicketPriority;
import com.smart_campus_hub.smart_campus_api.model.TicketStatus;
import com.smart_campus_hub.smart_campus_api.repository.AttachmentRepository;
import com.smart_campus_hub.smart_campus_api.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public TicketResponseDto createTicket(TicketCreateDto dto, List<MultipartFile> files, Long userId) {
        if (files != null && files.size() > 3) {
            throw new IllegalArgumentException("Maximum of 3 images can be uploaded per ticket.");
        }

        Ticket ticket = Ticket.builder()
                .resourceId(dto.getResourceId())
                .createdBy(userId)
                .category(dto.getCategory())
                .description(dto.getDescription())
                .priority(dto.getPriority())
                .contactDetails(dto.getContactDetails())
                .status(TicketStatus.OPEN)
                .build();

        ticket = ticketRepository.save(ticket);

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileUrl = fileStorageService.storeFile(file);
                    Attachment attachment = Attachment.builder()
                            .ticket(ticket)
                            .fileUrl(fileUrl)
                            .build();
                    attachmentRepository.save(attachment);
                    ticket.getAttachments().add(attachment);
                }
            }
        }

        return mapToResponse(ticket);
    }

    public List<TicketResponseDto> getAllTickets(TicketStatus status, TicketPriority priority, String category) {
        // Not using dynamic Specifications for simplicity, but doing manual filter over lists depending on DB size
        // If necessary, JPA Specifications should be used. For this module, we filter dynamically.
        List<Ticket> tickets = ticketRepository.findAll();

        if (status != null) {
            tickets = tickets.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }
        if (priority != null) {
            tickets = tickets.stream().filter(t -> t.getPriority() == priority).collect(Collectors.toList());
        }
        if (category != null && !category.isEmpty()) {
            tickets = tickets.stream().filter(t -> t.getCategory().equalsIgnoreCase(category)).collect(Collectors.toList());
        }

        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TicketResponseDto getTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponseDto updateStatus(Long id, TicketStatusUpdateDto dto, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"TECHNICIAN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Only authorized users can change status.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (dto.getStatus() == TicketStatus.IN_PROGRESS && ticket.getFirstResponseAt() == null) {
            ticket.setFirstResponseAt(LocalDateTime.now());
        }

        if (dto.getStatus() == TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        ticket.setStatus(dto.getStatus());
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponseDto assignTicket(Long id, TicketAssignDto dto, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can assign technicians.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticket.setAssignedTo(dto.getAssignTo());
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponseDto rejectTicket(Long id, TicketRejectDto dto, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can reject tickets.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticket.setStatus(TicketStatus.REJECTED);
        ticket.setResolutionNotes(dto.getReason());
        ticket.setResolvedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponseDto resolveTicket(Long id, TicketResolveDto dto, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"TECHNICIAN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Only authorized users can resolve tickets.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolutionNotes(dto.getResolutionNotes());
        if (ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    private TicketResponseDto mapToResponse(Ticket ticket) {
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(ticket.getId());
        dto.setResourceId(ticket.getResourceId());
        dto.setCreatedBy(ticket.getCreatedBy());
        dto.setAssignedTo(ticket.getAssignedTo());
        dto.setCategory(ticket.getCategory());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority());
        dto.setStatus(ticket.getStatus());
        dto.setContactDetails(ticket.getContactDetails());
        dto.setResolutionNotes(ticket.getResolutionNotes());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setFirstResponseAt(ticket.getFirstResponseAt());
        dto.setResolvedAt(ticket.getResolvedAt());

        // Calculate response time in minutes
        if (ticket.getFirstResponseAt() != null && ticket.getCreatedAt() != null) {
            dto.setResponseTimeMinutes(Duration.between(ticket.getCreatedAt(), ticket.getFirstResponseAt()).toMinutes());
        }

        // Calculate resolution time in minutes
        if (ticket.getResolvedAt() != null && ticket.getCreatedAt() != null) {
            dto.setResolutionTimeMinutes(Duration.between(ticket.getCreatedAt(), ticket.getResolvedAt()).toMinutes());
        }

        if (ticket.getComments() != null) {
            dto.setComments(ticket.getComments().stream().map(this::mapCommentToDto).collect(Collectors.toList()));
        }

        if (ticket.getAttachments() != null) {
            dto.setAttachments(ticket.getAttachments().stream().map(this::mapAttachmentToDto).collect(Collectors.toList()));
        }

        return dto;
    }

    private CommentResponseDto mapCommentToDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    private AttachmentDto mapAttachmentToDto(Attachment attachment) {
        AttachmentDto dto = new AttachmentDto();
        dto.setId(attachment.getId());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setUploadedAt(attachment.getUploadedAt());
        return dto;
    }
}
