package com.smart_campus_hub.smart_campus_api.service;

import com.smart_campus_hub.smart_campus_api.model.TicketPriority;

public class SlaPolicy {

    public record SlaLimits(long firstResponseMinutes, long resolutionMinutes) {}

    public static SlaLimits getLimits(TicketPriority priority) {
        return switch (priority) {
            case LOW      -> new SlaLimits(60,  480);
            case MEDIUM   -> new SlaLimits(30,  240);
            case HIGH     -> new SlaLimits(10,   60);
            case CRITICAL -> new SlaLimits(5,    30);
        };
    }
}
