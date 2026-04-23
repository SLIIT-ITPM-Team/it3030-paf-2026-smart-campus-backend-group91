package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.AvailabilityResponseDto;
import com.smart_campus_hub.smart_campus_api.dto.BookingCancelDto;
import com.smart_campus_hub.smart_campus_api.dto.BookingCreateDto;
import com.smart_campus_hub.smart_campus_api.dto.BookingRejectDto;
import com.smart_campus_hub.smart_campus_api.dto.BookingResponseDto;
import com.smart_campus_hub.smart_campus_api.model.BookingStatus;
import com.smart_campus_hub.smart_campus_api.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingCreateDto dto,
            
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            
            @RequestHeader(value = "X-User-Name", defaultValue = "Campus User") String userName,
            
            @RequestHeader(value = "X-User-Email", defaultValue = "student@smartcampus.edu") String userEmail) {
        
        
        return ResponseEntity.ok(bookingService.createBooking(dto, userId, userName, userEmail));
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponseDto> checkAvailability(
            
            @RequestParam Long resourceId,
            
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
            
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        
      
        return ResponseEntity.ok(bookingService.checkAvailability(resourceId, bookingDate, startTime, endTime));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(
            
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            
            @RequestParam(required = false) BookingStatus status,
            
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        
        return ResponseEntity.ok(bookingService.getMyBookings(userId, status, fromDate, toDate));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<BookingResponseDto>> getAllBookings(
            // Step 1: Ensure the requester's role is provided to verify authorization in the service
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole,
            // Step 2: Capture various optional filter parameters for the admin dashboard
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        // Step 3: Pass all filters to the service to retrieve the matching system-wide bookings
        return ResponseEntity.ok(bookingService.getAllBookings(status, resourceId, userId, fromDate, toDate, userRole));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBooking(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(bookingService.getBooking(id, userId, userRole));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<BookingResponseDto> approveBooking(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(bookingService.approveBooking(id, userRole));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<BookingResponseDto> rejectBooking(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookingRejectDto dto,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        return ResponseEntity.ok(bookingService.rejectBooking(id, dto, userRole));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(
            
            @PathVariable("id") Long id, 
            @RequestBody(required = false) BookingCancelDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        
        
        BookingCancelDto cancelDto = dto == null ? new BookingCancelDto() : dto;
        
        
        return ResponseEntity.ok(bookingService.cancelBooking(id, cancelDto, userId, userRole));
    }

    @PostMapping("/reminders/send-due")
    public ResponseEntity<List<BookingResponseDto>> sendDueReminders(
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can send reminders.");
        }
        return ResponseEntity.ok(bookingService.sendDueReminders());
    }
}
