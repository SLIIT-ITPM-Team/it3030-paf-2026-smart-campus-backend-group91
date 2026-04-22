package com.smart_campus_hub.smart_campus_api.controllers;

import com.smart_campus_hub.smart_campus_api.dto.BookingResponseDto;
import com.smart_campus_hub.smart_campus_api.dto.WaitlistCreateDto;
import com.smart_campus_hub.smart_campus_api.dto.WaitlistResponseDto;
import com.smart_campus_hub.smart_campus_api.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/waitlists")
public class WaitlistController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<WaitlistResponseDto> joinWaitlist(
            @Valid @RequestBody WaitlistCreateDto dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "Campus User") String userName,
            @RequestHeader(value = "X-User-Email", defaultValue = "student@smartcampus.edu") String userEmail) {
        return ResponseEntity.ok(bookingService.joinWaitlist(dto, userId, userName, userEmail));
    }

    @GetMapping("/my")
    public ResponseEntity<List<WaitlistResponseDto>> getMyWaitlist(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(bookingService.getMyWaitlist(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WaitlistResponseDto> leaveWaitlist(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(bookingService.leaveWaitlist(id, userId));
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<BookingResponseDto> claimWaitlistSlot(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(bookingService.claimWaitlistSlot(id, userId));
    }
}
