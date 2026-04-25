package com.smart_campus_hub.smart_campus_api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.smart_campus_hub.smart_campus_api.model.ResourceStatus;
import com.smart_campus_hub.smart_campus_api.repository.BookingRepository;
import com.smart_campus_hub.smart_campus_api.repository.CampusResourceRepository;

@Service
public class AnalyticsService {

    private final BookingRepository bookingRepository;
    private final CampusResourceRepository campusResourceRepository;

    public AnalyticsService(BookingRepository bookingRepository, CampusResourceRepository campusResourceRepository) {
        this.bookingRepository = bookingRepository;
        this.campusResourceRepository = campusResourceRepository;
    }

    public AnalyticsSummary getSummary() {
        long totalBookingsToday = bookingRepository.countByBookingDate(LocalDate.now());
        long activeResources = campusResourceRepository.findByStatus(ResourceStatus.ACTIVE).size();
        List<TopResource> topResources = bookingRepository.findTopResourcesByBookingCount()
                .stream()
                .map(view -> new TopResource(view.getResourceId(), view.getName(), view.getBookingCount()))
                .collect(Collectors.toList());
        return new AnalyticsSummary(totalBookingsToday, activeResources, topResources);
    }

    public static class AnalyticsSummary {
        private long totalBookingsToday;
        private long activeResources;
        private List<TopResource> topResources;

        public AnalyticsSummary(long totalBookingsToday, long activeResources, List<TopResource> topResources) {
            this.totalBookingsToday = totalBookingsToday;
            this.activeResources = activeResources;
            this.topResources = topResources;
        }

        public long getTotalBookingsToday() {
            return totalBookingsToday;
        }

        public long getActiveResources() {
            return activeResources;
        }

        public List<TopResource> getTopResources() {
            return topResources;
        }
    }

    public static class TopResource {
        private Long resourceId;
        private String name;
        private Long bookingCount;

        public TopResource(Long resourceId, String name, Long bookingCount) {
            this.resourceId = resourceId;
            this.name = name;
            this.bookingCount = bookingCount;
        }

        public Long getResourceId() {
            return resourceId;
        }

        public String getName() {
            return name;
        }

        public Long getBookingCount() {
            return bookingCount;
        }
    }
}
