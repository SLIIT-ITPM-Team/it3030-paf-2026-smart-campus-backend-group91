package com.smart_campus_hub.smart_campus_api.config;

import com.smart_campus_hub.smart_campus_api.model.CampusResource;
import com.smart_campus_hub.smart_campus_api.model.ResourceStatus;
import com.smart_campus_hub.smart_campus_api.model.ResourceType;
import com.smart_campus_hub.smart_campus_api.repository.CampusResourceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BookingDataInitializer {

    @Bean
    CommandLineRunner seedBookingResources(CampusResourceRepository campusResourceRepository) {
        return args -> {
            if (campusResourceRepository.count() > 0) {
                return;
            }

            campusResourceRepository.saveAll(List.of(
                    CampusResource.builder()
                            .name("Lecture Hall A1")
                            .type(ResourceType.LECTURE_HALL)
                            .location("Main Academic Block - Floor 1")
                            .capacity(180)
                            .status(ResourceStatus.ACTIVE)
                            .description("Tiered lecture space with projector, podium audio, and hybrid teaching capture.")
                            .build(),
                    CampusResource.builder()
                            .name("Data Science Lab")
                            .type(ResourceType.LAB)
                            .location("Innovation Center - Floor 3")
                            .capacity(45)
                            .status(ResourceStatus.ACTIVE)
                            .description("GPU workstations, dual displays, whiteboards, and controlled access.")
                            .build(),
                    CampusResource.builder()
                            .name("Meeting Room C2")
                            .type(ResourceType.MEETING_ROOM)
                            .location("Admin Building - Floor 2")
                            .capacity(12)
                            .status(ResourceStatus.ACTIVE)
                            .description("Conference room with video meeting kit and wall display.")
                            .build(),
                    CampusResource.builder()
                            .name("4K Projector PJ-9")
                            .type(ResourceType.EQUIPMENT)
                            .location("Equipment Store")
                            .capacity(0)
                            .status(ResourceStatus.ACTIVE)
                            .description("Portable projector kit with HDMI, USB-C adapter, and carrying case.")
                            .build(),
                    CampusResource.builder()
                            .name("DSLR Camera CAM-3")
                            .type(ResourceType.EQUIPMENT)
                            .location("Media Unit")
                            .capacity(0)
                            .status(ResourceStatus.OUT_OF_SERVICE)
                            .description("Temporarily unavailable while the lens mount is being repaired.")
                            .build()));
        };
    }
}
