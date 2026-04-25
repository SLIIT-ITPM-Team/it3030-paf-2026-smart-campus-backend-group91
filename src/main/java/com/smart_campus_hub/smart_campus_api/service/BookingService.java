package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.dto.AvailabilityResponseDto;
import com.smart_campus_hub.smart_campus_api.dto.BookingCancelDto;
import com.smart_campus_hub.smart_campus_api.dto.BookingCreateDto;
import com.smart_campus_hub.smart_campus_api.dto.BookingRejectDto;
import com.smart_campus_hub.smart_campus_api.dto.BookingResponseDto;
import com.smart_campus_hub.smart_campus_api.dto.WaitlistCreateDto;
import com.smart_campus_hub.smart_campus_api.dto.WaitlistResponseDto;
import com.smart_campus_hub.smart_campus_api.entity.Location;
import com.smart_campus_hub.smart_campus_api.entity.Resource;
import com.smart_campus_hub.smart_campus_api.model.Booking;
import com.smart_campus_hub.smart_campus_api.model.BookingStatus;
import com.smart_campus_hub.smart_campus_api.model.WaitlistEntry;
import com.smart_campus_hub.smart_campus_api.model.WaitlistStatus;
import com.smart_campus_hub.smart_campus_api.repository.BookingRepository;
import com.smart_campus_hub.smart_campus_api.repository.ResourceRepository;
import com.smart_campus_hub.smart_campus_api.repository.WaitlistEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES = Arrays.asList(BookingStatus.PENDING, BookingStatus.APPROVED);
    private static final List<WaitlistStatus> OPEN_WAITLIST_STATUSES = Arrays.asList(WaitlistStatus.WAITING, WaitlistStatus.NOTIFIED);
    private static final LocalTime DAY_START = LocalTime.of(8, 0);
    private static final LocalTime DAY_END = LocalTime.of(20, 0);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WaitlistEntryRepository waitlistEntryRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Transactional
    public BookingResponseDto createBooking(BookingCreateDto dto, Long userId, String userName, String userEmail) {
        validateTimeRange(dto.getBookingDate(), dto.getStartTime(), dto.getEndTime());
        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        validateBookableResource(resource, dto.getExpectedAttendees());

        if (hasConflict(resource.getId(), dto.getBookingDate(), dto.getStartTime(), dto.getEndTime(), BLOCKING_STATUSES)) {
            throw new IllegalArgumentException("The selected slot is unavailable for this resource.");
        }

        Booking booking = Booking.builder()
                .resource(resource)
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .bookingDate(dto.getBookingDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .purpose(dto.getPurpose())
                .expectedAttendees(dto.getExpectedAttendees())
                .status(BookingStatus.PENDING)
                .reminderSent(false)
                .build();

        booking = bookingRepository.save(booking);
        emailNotificationService.send(
                booking.getUserEmail(),
                "Booking request submitted: BK-" + booking.getId(),
                bookingEmailBody(booking, "Your booking request was submitted with PENDING status."));
        return mapBooking(booking);
    }

    public AvailabilityResponseDto checkAvailability(Long resourceId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        validateTimeRange(bookingDate, startTime, endTime);
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        AvailabilityResponseDto response = new AvailabilityResponseDto();
        if ("OUT_OF_SERVICE".equalsIgnoreCase(resource.getStatus())) {
            response.setAvailable(false);
            response.setMessage("Resource is out of service.");
            response.setSuggestions(List.of());
            return response;
        }

        boolean available = !hasConflict(resourceId, bookingDate, startTime, endTime, BLOCKING_STATUSES);
        response.setAvailable(available);
        response.setMessage(available ? "Slot is available." : "Slot is already taken.");
        response.setSuggestions(available ? List.of() : suggestSlots(resourceId, bookingDate, startTime, endTime));
        return response;
    }

    public List<BookingResponseDto> getMyBookings(Long userId, BookingStatus status, LocalDate fromDate, LocalDate toDate) {
        return bookingRepository.searchBookings(status, null, userId, fromDate, toDate)
                .stream()
                .map(this::mapBooking)
                .collect(Collectors.toList());
    }

    public List<BookingResponseDto> getAllBookings(
            BookingStatus status,
            Long resourceId,
            Long userId,
            LocalDate fromDate,
            LocalDate toDate,
            String userRole) {
        requireAdmin(userRole);
        return bookingRepository.searchBookings(status, resourceId, userId, fromDate, toDate)
                .stream()
                .map(this::mapBooking)
                .collect(Collectors.toList());
    }

    public BookingResponseDto getBooking(Long id, Long userId, String userRole) {
        Booking booking = findBooking(id);
        requireOwnerOrAdmin(booking, userId, userRole);
        return mapBooking(booking);
    }

    @Transactional
    public BookingResponseDto approveBooking(Long id, String userRole) {
        requireAdmin(userRole);
        Booking booking = findBooking(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING bookings can be approved.");
        }
        if (hasConflict(booking.getResource().getId(), booking.getBookingDate(), booking.getStartTime(), booking.getEndTime(), List.of(BookingStatus.APPROVED))) {
            throw new IllegalArgumentException("Cannot approve because another APPROVED booking overlaps this slot.");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setQrCodeValue(generateQrCodeValue(booking));
        booking.setQrGeneratedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        emailNotificationService.send(
                booking.getUserEmail(),
                "Booking approved: BK-" + booking.getId(),
                bookingEmailBody(booking, "Your booking was approved. QR code: " + booking.getQrCodeValue()));
        return mapBooking(booking);
    }

    @Transactional
    public BookingResponseDto rejectBooking(Long id, BookingRejectDto dto, String userRole) {
        requireAdmin(userRole);
        Booking booking = findBooking(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING bookings can be rejected.");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(dto.getReason());
        booking.setQrCodeValue(null);
        booking.setQrGeneratedAt(null);
        booking = bookingRepository.save(booking);

        emailNotificationService.send(
                booking.getUserEmail(),
                "Booking rejected: BK-" + booking.getId(),
                bookingEmailBody(booking, "Your booking was rejected. Reason: " + dto.getReason()));
        return mapBooking(booking);
    }

    @Transactional
    public BookingResponseDto cancelBooking(Long id, BookingCancelDto dto, Long userId, String userRole) {
        Booking booking = findBooking(id);
        boolean adminCancel = "ADMIN".equalsIgnoreCase(userRole);
        boolean ownerCancel = booking.getUserId().equals(userId);

        if (!adminCancel && !ownerCancel) {
            throw new org.springframework.security.access.AccessDeniedException("You can cancel only your own booking.");
        }
        if (adminCancel && booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalArgumentException("Admin can cancel only APPROVED bookings.");
        }
        if (ownerCancel && booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalArgumentException("Only PENDING or APPROVED bookings can be cancelled.");
        }

        BookingStatus previousStatus = booking.getStatus();
        String reason = dto.getReason() == null || dto.getReason().isBlank()
                ? (adminCancel ? "Cancelled by Admin." : "Cancelled by requester.")
                : dto.getReason();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setQrCodeValue(null);
        booking.setQrGeneratedAt(null);
        booking = bookingRepository.save(booking);

        emailNotificationService.send(
                booking.getUserEmail(),
                "Booking cancelled: BK-" + booking.getId(),
                bookingEmailBody(booking, "Your booking was cancelled. Reason: " + reason));

        if (previousStatus == BookingStatus.APPROVED) {
            notifyFirstWaitlistEntry(booking);
        }
        return mapBooking(booking);
    }

    @Transactional
    public WaitlistResponseDto joinWaitlist(WaitlistCreateDto dto, Long userId, String userName, String userEmail) {
        validateTimeRange(dto.getBookingDate(), dto.getStartTime(), dto.getEndTime());
        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        if ("OUT_OF_SERVICE".equalsIgnoreCase(resource.getStatus())) {
            throw new IllegalArgumentException("Cannot join waitlist for an out of service resource.");
        }
        if (!hasConflict(resource.getId(), dto.getBookingDate(), dto.getStartTime(), dto.getEndTime(), BLOCKING_STATUSES)) {
            throw new IllegalArgumentException("This slot is available, so a waitlist is not needed.");
        }

        waitlistEntryRepository.findByResourceIdAndBookingDateAndStartTimeAndEndTimeAndUserIdAndStatusIn(
                        resource.getId(), dto.getBookingDate(), dto.getStartTime(), dto.getEndTime(), userId, OPEN_WAITLIST_STATUSES)
                .ifPresent(entry -> {
                    throw new IllegalArgumentException("You have already joined the waitlist for this slot.");
                });

        WaitlistEntry entry = WaitlistEntry.builder()
                .resource(resource)
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .bookingDate(dto.getBookingDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(WaitlistStatus.WAITING)
                .build();

        entry = waitlistEntryRepository.save(entry);
        return mapWaitlist(entry);
    }

    public List<WaitlistResponseDto> getMyWaitlist(Long userId) {
        return waitlistEntryRepository.findByUserIdOrderByCreatedAtAsc(userId)
                .stream()
                .map(this::mapWaitlist)
                .collect(Collectors.toList());
    }

    @Transactional
    public WaitlistResponseDto leaveWaitlist(Long waitlistId, Long userId) {
        WaitlistEntry entry = findWaitlistEntry(waitlistId);
        if (!entry.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can leave only your own waitlist entry.");
        }
        if (entry.getStatus() == WaitlistStatus.CLAIMED || entry.getStatus() == WaitlistStatus.LEFT) {
            throw new IllegalArgumentException("This waitlist entry is already closed.");
        }
        entry.setStatus(WaitlistStatus.LEFT);
        return mapWaitlist(waitlistEntryRepository.save(entry));
    }

    @Transactional
    public BookingResponseDto claimWaitlistSlot(Long waitlistId, Long userId) {
        WaitlistEntry entry = findWaitlistEntry(waitlistId);
        if (!entry.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can claim only your own waitlist entry.");
        }
        if (entry.getStatus() != WaitlistStatus.NOTIFIED) {
            throw new IllegalArgumentException("This waitlist entry has not been notified for claiming.");
        }
        if (entry.getClaimExpiresAt() == null || entry.getClaimExpiresAt().isBefore(LocalDateTime.now())) {
            entry.setStatus(WaitlistStatus.EXPIRED);
            waitlistEntryRepository.save(entry);
            notifyNextAfterExpired(entry);
            throw new IllegalArgumentException("The 2 hour claim window has expired.");
        }
        if (hasConflict(entry.getResource().getId(), entry.getBookingDate(), entry.getStartTime(), entry.getEndTime(), BLOCKING_STATUSES)) {
            throw new IllegalArgumentException("This slot is no longer available.");
        }

        Booking booking = Booking.builder()
                .resource(entry.getResource())
                .userId(entry.getUserId())
                .userName(entry.getUserName())
                .userEmail(entry.getUserEmail())
                .bookingDate(entry.getBookingDate())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .purpose("Claimed from waitlist")
                .expectedAttendees(0)
                .status(BookingStatus.APPROVED)
                .reminderSent(false)
                .build();
        booking = bookingRepository.save(booking);
        booking.setQrCodeValue(generateQrCodeValue(booking));
        booking.setQrGeneratedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        entry.setStatus(WaitlistStatus.CLAIMED);
        waitlistEntryRepository.save(entry);

        emailNotificationService.send(
                booking.getUserEmail(),
                "Waitlist slot claimed: BK-" + booking.getId(),
                bookingEmailBody(booking, "Your waitlist slot was claimed and approved. QR code: " + booking.getQrCodeValue()));
        return mapBooking(booking);
    }

    @Transactional
    public List<BookingResponseDto> sendDueReminders() {
        LocalDateTime target = LocalDateTime.now().plusHours(1);
        LocalTime from = target.toLocalTime().withSecond(0).withNano(0);
        LocalTime to = from.plusMinutes(1);

        List<Booking> dueBookings = bookingRepository.findApprovedBookingsDueForReminder(
                BookingStatus.APPROVED,
                target.toLocalDate(),
                from,
                to);

        return dueBookings.stream()
                .map(booking -> {
                    booking.setReminderSent(true);
                    Booking saved = bookingRepository.save(booking);
                    emailNotificationService.send(
                            saved.getUserEmail(),
                            "Booking reminder: BK-" + saved.getId(),
                            bookingEmailBody(saved, "Reminder: your approved booking starts in 1 hour."));
                    return mapBooking(saved);
                })
                .collect(Collectors.toList());
    }

    private void notifyFirstWaitlistEntry(Booking booking) {
        List<WaitlistEntry> entries = waitlistEntryRepository
                .findByResourceIdAndBookingDateAndStartTimeAndEndTimeAndStatusInOrderByCreatedAtAsc(
                        booking.getResource().getId(),
                        booking.getBookingDate(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        List.of(WaitlistStatus.WAITING));

        if (entries.isEmpty()) {
            return;
        }

        WaitlistEntry first = entries.get(0);
        LocalDateTime notifiedAt = LocalDateTime.now();
        first.setStatus(WaitlistStatus.NOTIFIED);
        first.setNotifiedAt(notifiedAt);
        first.setClaimExpiresAt(notifiedAt.plusHours(2));
        first = waitlistEntryRepository.save(first);
        emailNotificationService.send(
                first.getUserEmail(),
                "Booking slot opened",
                "A slot opened for " + first.getResource().getName() + " on " + first.getBookingDate()
                        + " from " + first.getStartTime() + " to " + first.getEndTime()
                        + ". You have 2 hours to claim it.");
    }

    private void notifyNextAfterExpired(WaitlistEntry expiredEntry) {
        Booking syntheticBooking = Booking.builder()
                .resource(expiredEntry.getResource())
                .bookingDate(expiredEntry.getBookingDate())
                .startTime(expiredEntry.getStartTime())
                .endTime(expiredEntry.getEndTime())
                .build();
        notifyFirstWaitlistEntry(syntheticBooking);
    }

    private boolean hasConflict(Long resourceId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime, List<BookingStatus> statuses) {
        return !bookingRepository.findConflicts(resourceId, bookingDate, startTime, endTime, statuses).isEmpty();
    }

    private List<AvailabilityResponseDto.AvailableSlotDto> suggestSlots(
            Long resourceId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime) {
        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return java.util.stream.Stream.iterate(DAY_START, time -> !time.plusMinutes(durationMinutes).isAfter(DAY_END), time -> time.plusMinutes(30))
                .filter(candidateStart -> !isPastSlot(bookingDate, candidateStart))
                .filter(candidateStart -> !hasConflict(resourceId, bookingDate, candidateStart, candidateStart.plusMinutes(durationMinutes), BLOCKING_STATUSES))
                .limit(3)
                .map(candidateStart -> {
                    AvailabilityResponseDto.AvailableSlotDto dto = new AvailabilityResponseDto.AvailableSlotDto();
                    dto.setStartTime(candidateStart);
                    dto.setEndTime(candidateStart.plusMinutes(durationMinutes));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void validateTimeRange(LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (isPastSlot(bookingDate, startTime)) {
            throw new IllegalArgumentException("Cannot book a resource for a past date or time.");
        }
    }

    private boolean isPastSlot(LocalDate bookingDate, LocalTime startTime) {
        LocalDate today = LocalDate.now();
        return bookingDate.isBefore(today) || (bookingDate.isEqual(today) && startTime.isBefore(LocalTime.now()));
    }

    private void validateBookableResource(Resource resource, Integer attendees) {
        if ("OUT_OF_SERVICE".equalsIgnoreCase(resource.getStatus())) {
            throw new IllegalArgumentException("Resource is out of service and cannot be booked.");
        }
        if (attendees != null && resource.getCapacity() != null && resource.getCapacity() > 0 && attendees > resource.getCapacity()) {
            throw new IllegalArgumentException("Expected attendees exceed resource capacity.");
        }
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    private WaitlistEntry findWaitlistEntry(Long id) {
        return waitlistEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));
    }

    private void requireAdmin(String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can perform this action.");
        }
    }

    private void requireOwnerOrAdmin(Booking booking, Long userId, String userRole) {
        if (!booking.getUserId().equals(userId) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new org.springframework.security.access.AccessDeniedException("You can view only your own booking.");
        }
    }

    private String generateQrCodeValue(Booking booking) {
        String resourceName = booking.getResource() == null ? "Resource" : booking.getResource().getName();
        return "BK-" + booking.getId() + "|RES-" + booking.getResource().getId() + "|" + resourceName + "|"
                + booking.getBookingDate() + "|" + booking.getStartTime() + "-" + booking.getEndTime() + "|"
                + UUID.randomUUID();
    }

    private String bookingEmailBody(Booking booking, String message) {
        return message + "\nBooking ID: BK-" + booking.getId()
                + "\nResource: " + booking.getResource().getName()
                + "\nDate: " + booking.getBookingDate()
                + "\nTime: " + booking.getStartTime() + " - " + booking.getEndTime();
    }

    private String formatLocation(Location location) {
        if (location == null) return null;
        StringBuilder sb = new StringBuilder();
        if (location.getBuildingName() != null && !location.getBuildingName().isBlank()) sb.append(location.getBuildingName());
        if (location.getFloorNumber() != null && !location.getFloorNumber().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Floor ").append(location.getFloorNumber());
        }
        if (location.getRoomNumber() != null && !location.getRoomNumber().isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(location.getRoomNumber());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private BookingResponseDto mapBooking(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(booking.getId());
        dto.setResourceId(booking.getResource().getId());
        dto.setResourceName(booking.getResource().getName());
        dto.setResourceLocation(formatLocation(booking.getResource().getLocation()));
        dto.setUserId(booking.getUserId());
        dto.setUserName(booking.getUserName());
        dto.setUserEmail(booking.getUserEmail());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setPurpose(booking.getPurpose());
        dto.setExpectedAttendees(booking.getExpectedAttendees());
        dto.setStatus(booking.getStatus());
        dto.setRejectionReason(booking.getRejectionReason());
        dto.setCancellationReason(booking.getCancellationReason());
        dto.setQrCodeValue(booking.getQrCodeValue());
        dto.setQrGeneratedAt(booking.getQrGeneratedAt());
        dto.setReminderSent(booking.isReminderSent());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        return dto;
    }

    private WaitlistResponseDto mapWaitlist(WaitlistEntry entry) {
        WaitlistResponseDto dto = new WaitlistResponseDto();
        dto.setWaitlistId(entry.getId());
        dto.setResourceId(entry.getResource().getId());
        dto.setResourceName(entry.getResource().getName());
        dto.setUserId(entry.getUserId());
        dto.setUserName(entry.getUserName());
        dto.setUserEmail(entry.getUserEmail());
        dto.setBookingDate(entry.getBookingDate());
        dto.setStartTime(entry.getStartTime());
        dto.setEndTime(entry.getEndTime());
        dto.setStatus(entry.getStatus());
        dto.setPosition(calculatePosition(entry));
        dto.setNotifiedAt(entry.getNotifiedAt());
        dto.setClaimExpiresAt(entry.getClaimExpiresAt());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        return dto;
    }

    private Integer calculatePosition(WaitlistEntry entry) {
        List<WaitlistEntry> queue = waitlistEntryRepository
                .findByResourceIdAndBookingDateAndStartTimeAndEndTimeAndStatusInOrderByCreatedAtAsc(
                        entry.getResource().getId(),
                        entry.getBookingDate(),
                        entry.getStartTime(),
                        entry.getEndTime(),
                        OPEN_WAITLIST_STATUSES);
        for (int index = 0; index < queue.size(); index++) {
            if (queue.get(index).getId().equals(entry.getId())) {
                return index + 1;
            }
        }
        return null;
    }
}
