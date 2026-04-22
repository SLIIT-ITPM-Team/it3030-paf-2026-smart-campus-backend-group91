package com.smart_campus_hub.smart_campus_api.repository;

import com.smart_campus_hub.smart_campus_api.model.Booking;
import com.smart_campus_hub.smart_campus_api.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByBookingDateAscStartTimeAsc(Long userId);

    @Query("""
            select b from Booking b
            where b.resource.id = :resourceId
              and b.bookingDate = :bookingDate
              and b.status in :statuses
              and b.startTime < :endTime
              and b.endTime > :startTime
            """)
    List<Booking> findConflicts(
            @Param("resourceId") Long resourceId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<BookingStatus> statuses);

    @Query("""
            select b from Booking b
            where (:status is null or b.status = :status)
              and (:resourceId is null or b.resource.id = :resourceId)
              and (:userId is null or b.userId = :userId)
              and (:fromDate is null or b.bookingDate >= :fromDate)
              and (:toDate is null or b.bookingDate <= :toDate)
            order by b.bookingDate asc, b.startTime asc
            """)
    List<Booking> searchBookings(
            @Param("status") BookingStatus status,
            @Param("resourceId") Long resourceId,
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("""
            select b from Booking b
            where b.status = :status
              and b.reminderSent = false
              and b.bookingDate = :bookingDate
              and b.startTime between :fromTime and :toTime
            """)
    List<Booking> findApprovedBookingsDueForReminder(
            @Param("status") BookingStatus status,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime);
}
