package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.Tickets.*;
import com.smart_campus_hub.smart_campus_api.entity.User;
import com.smart_campus_hub.smart_campus_api.exception.ResourceNotFoundException;
import com.smart_campus_hub.smart_campus_api.exception.UnauthorizedException;
import com.smart_campus_hub.smart_campus_api.model.Attachment;
import com.smart_campus_hub.smart_campus_api.model.Comment;
import com.smart_campus_hub.smart_campus_api.model.Ticket;
import com.smart_campus_hub.smart_campus_api.model.TicketPriority;
import com.smart_campus_hub.smart_campus_api.model.TicketStatus;
import com.smart_campus_hub.smart_campus_api.repository.AttachmentRepository;
import com.smart_campus_hub.smart_campus_api.repository.TicketRepository;
import com.smart_campus_hub.smart_campus_api.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public TicketResponse createTicket(TicketCreateRequest req, List<MultipartFile> files, Long userId) {
        if (files != null && files.size() > 3) {
            throw new IllegalArgumentException("Maximum of 3 images can be uploaded per ticket.");
        }

        Ticket ticket = Ticket.builder()
                .resourceId(req.getResourceId())
                .createdBy(userId)
                .category(req.getCategory())
                .description(req.getDescription())
                .priority(req.getPriority())
                .contactDetails(req.getContactDetails())
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

    public List<TicketResponse> getAllTickets(Long userId, String userRole,
                                              TicketStatus status, TicketPriority priority, String category) {
        List<Ticket> tickets;

        if ("ADMIN".equalsIgnoreCase(userRole)) {
            tickets = ticketRepository.findAll();
        } else if ("TECHNICIAN".equalsIgnoreCase(userRole)) {
            tickets = ticketRepository.findByAssignedTo(userId);
        } else {
            tickets = ticketRepository.findByCreatedBy(userId);
        }

        if (status != null) {
            tickets = tickets.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }
        if (priority != null) {
            tickets = tickets.stream().filter(t -> t.getPriority() == priority).collect(Collectors.toList());
        }
        if (category != null && !category.isEmpty()) {
            tickets = tickets.stream().filter(t -> category.equalsIgnoreCase(t.getCategory())).collect(Collectors.toList());
        }

        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TicketResponse> getMyTickets(Long userId) {
        return ticketRepository.findByCreatedBy(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse getTicket(Long id, Long userId, String userRole) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        if ("USER".equalsIgnoreCase(userRole) && !ticket.getCreatedBy().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this ticket.");
        }

        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long id, TicketStatusUpdateRequest req, Long userId, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"TECHNICIAN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Only authorized users can change ticket status.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        if ("TECHNICIAN".equalsIgnoreCase(userRole) &&
                (ticket.getAssignedTo() == null || !ticket.getAssignedTo().equals(userId))) {
            throw new UnauthorizedException("You can only update tickets assigned to you.");
        }

        if (req.getStatus() == TicketStatus.IN_PROGRESS) {
            applyFirstResponse(ticket);
        }

        if (req.getStatus() == TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.RESOLVED) {
            applyResolution(ticket);
            if (req.getResolutionNotes() != null) {
                ticket.setResolutionNotes(req.getResolutionNotes());
            }
        }

        ticket.setStatus(req.getStatus());
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(Long id, TicketAssignRequest req, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Only admins can assign technicians.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        ticket.setAssignedTo(req.getAssignedToId());
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            applyFirstResponse(ticket);
        }

        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse rejectTicket(Long id, TicketRejectRequest req, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Only admins can reject tickets.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        ticket.setStatus(TicketStatus.REJECTED);
        ticket.setResolutionNotes(req.getReason());
        applyFirstResponse(ticket);
        applyResolution(ticket);
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse resolveTicket(Long id, TicketResolveRequest req, Long userId, String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole) && !"TECHNICIAN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Only authorized users can resolve tickets.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolutionNotes(req.getResolutionNotes());
        applyResolution(ticket);
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse declineAssignment(Long id, Long userId, String userRole) {
        if (!"TECHNICIAN".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedException("Only technicians can decline assignments.");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        if (!userId.equals(ticket.getAssignedTo())) {
            throw new UnauthorizedException("You are not assigned to this ticket.");
        }

        ticket.setAssignedTo(null);
        ticket.setStatus(TicketStatus.OPEN);
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    private void applyFirstResponse(Ticket ticket) {
        if (ticket.getFirstResponseAt() != null) return;

        LocalDateTime now = LocalDateTime.now();
        ticket.setFirstResponseAt(now);

        long elapsed = Duration.between(ticket.getCreatedAt(), now).toMinutes();
        ticket.setTimeToFirstResponseMinutes(elapsed);

        SlaPolicy.SlaLimits limits = SlaPolicy.getLimits(ticket.getPriority());
        ticket.setFirstResponseSlaMet(elapsed <= limits.firstResponseMinutes());
    }

    private void applyResolution(Ticket ticket) {
        if (ticket.getResolvedAt() != null) return;

        LocalDateTime now = LocalDateTime.now();
        ticket.setResolvedAt(now);

        long elapsed = Duration.between(ticket.getCreatedAt(), now).toMinutes();
        ticket.setTimeToResolutionMinutes(elapsed);

        SlaPolicy.SlaLimits limits = SlaPolicy.getLimits(ticket.getPriority());
        ticket.setResolutionSlaMet(elapsed <= limits.resolutionMinutes());
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        String createdByName = userRepository.findById(ticket.getCreatedBy())
                .map(User::getUsername).orElse("Unknown");

        String assignedToName = ticket.getAssignedTo() == null ? null :
                userRepository.findById(ticket.getAssignedTo())
                        .map(User::getUsername).orElse(null);

        SlaPolicy.SlaLimits limits = SlaPolicy.getLimits(ticket.getPriority());

        return TicketResponse.builder()
                .id(ticket.getId())
                .resourceId(ticket.getResourceId())
                .createdBy(ticket.getCreatedBy())
                .createdByName(createdByName)
                .assignedTo(ticket.getAssignedTo())
                .assignedToName(assignedToName)
                .category(ticket.getCategory())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .contactDetails(ticket.getContactDetails())
                .resolutionNotes(ticket.getResolutionNotes())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .firstResponseAt(ticket.getFirstResponseAt())
                .resolvedAt(ticket.getResolvedAt())
                .timeToFirstResponseMinutes(ticket.getTimeToFirstResponseMinutes())
                .timeToResolutionMinutes(ticket.getTimeToResolutionMinutes())
                .firstResponseSlaMet(ticket.getFirstResponseSlaMet())
                .resolutionSlaMet(ticket.getResolutionSlaMet())
                .slaFirstResponseMinutes(limits.firstResponseMinutes())
                .slaResolutionMinutes(limits.resolutionMinutes())
                .attachments(ticket.getAttachments() == null ? List.of() :
                        ticket.getAttachments().stream().map(this::mapAttachment).collect(Collectors.toList()))
                .comments(ticket.getComments() == null ? List.of() :
                        ticket.getComments().stream().map(this::mapComment).collect(Collectors.toList()))
                .build();
    }

    private AttachmentResponse mapAttachment(Attachment a) {
        return AttachmentResponse.builder()
                .id(a.getId())
                .fileUrl(a.getFileUrl())
                .uploadedAt(a.getUploadedAt())
                .build();
    }

    private CommentResponse mapComment(Comment c) {
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
