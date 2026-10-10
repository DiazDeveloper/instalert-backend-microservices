package com.instalert_backend.incidents.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "CreateIncidentRequest",
        description = "Request payload for creating a new incident report",
        example = """
                {
                  "userId": 101,
                  "type": "Robbery",
                  "severity": "HIGH",
                  "address": "Jr. Moquegua 240",
                  "description": "Two armed suspects assaulted a pedestrian",
                  "status": "ACTIVE",
                  "latitude": -12.121,
                  "longitude": -77.029,
                  "timeReported": "2025-05-20T14:32:00.000Z"
                }
                """
)
public record CreateIncidentResource(

        @NotNull(message = "is required")
        @Schema(description = "User identifier", example = "101")
        Long userId,

        @NotBlank(message = "is required")
        @Schema(description = "Incident type", example = "Robbery")
        String type,

        @NotBlank(message = "is required")
        @Schema(description = "Incident severity level", example = "HIGH")
        String severity,

        @NotBlank(message = "is required")
        @Schema(description = "Incident address", example = "Jr. Moquegua 240")
        String address,

        @NotBlank(message = "is required")
        @Schema(description = "Incident description", example = "Two armed suspects assaulted a pedestrian")
        String description,

        @NotBlank(message = "is required")
        @Schema(description = "Incident status", example = "ACTIVE")
        String status,

        @NotNull(message = "is required")
        @Schema(description = "Latitude coordinate", example = "-12.121")
        Double latitude,

        @NotNull(message = "is required")
        @Schema(description = "Longitude coordinate", example = "-77.029")
        Double longitude,

        @NotBlank(message = "is required")
        @Schema(description = "Incident report timestamp", example = "2025-05-20T14:32:00.000Z")
        String timeReported
) {
}