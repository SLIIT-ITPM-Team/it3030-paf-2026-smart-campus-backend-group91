package com.smart_campus_hub.smart_campus_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart_campus_hub.smart_campus_api.service.AnalyticsService;
import com.smart_campus_hub.smart_campus_api.service.AnalyticsService.AnalyticsSummary;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsSummary> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }
}
