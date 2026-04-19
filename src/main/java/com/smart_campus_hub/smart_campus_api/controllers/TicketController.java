package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.*;
import com.smart_campus_hub.smart_campus_api.model.TicketPriority;
import com.smart_campus_hub.smart_campus_api.model.TicketStatus;
import com.smart_campus_hub.smart_campus_api.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketResponseDto> createTicket(
            @RequestPart("ticket") @Valid TicketCreateDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(ticketService.createTicket(dto, files, userId));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDto>> getTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ticketService.getAllTickets(status, priority, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> getTicketById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponseDto> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody TicketStatusUpdateDto dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(ticketService.updateStatus(id, dto, userRole));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponseDto> assignTicket(
            @PathVariable("id") Long id,
            @Valid @RequestBody TicketAssignDto dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(ticketService.assignTicket(id, dto, userRole));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<TicketResponseDto> rejectTicket(
            @PathVariable("id") Long id,
            @Valid @RequestBody TicketRejectDto dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(ticketService.rejectTicket(id, dto, userRole));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<TicketResponseDto> resolveTicket(
            @PathVariable("id") Long id,
            @Valid @RequestBody TicketResolveDto dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(ticketService.resolveTicket(id, dto, userRole));
    }
}
