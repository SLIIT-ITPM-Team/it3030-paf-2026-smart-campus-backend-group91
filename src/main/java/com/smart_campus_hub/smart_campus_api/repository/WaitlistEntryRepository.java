package com.smart_campus_hub.smart_campus_api.repository;

import com.smart_campus_hub.smart_campus_api.model.WaitlistEntry;
import com.smart_campus_hub.smart_campus_api.model.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
    List<WaitlistEntry> findByUserIdOrderByCreatedAtAsc(Long userId);

    List<WaitlistEntry> findByResourceIdAndBookingDateAndStartTimeAndEndTimeAndStatusInOrderByCreatedAtAsc(
            Long resourceId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime,
            Collection<WaitlistStatus> statuses);

    Optional<WaitlistEntry> findByResourceIdAndBookingDateAndStartTimeAndEndTimeAndUserIdAndStatusIn(
            Long resourceId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime,
            Long userId,
            Collection<WaitlistStatus> statuses);
}
