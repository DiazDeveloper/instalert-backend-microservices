package com.instalert_backend.incidents.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resource for an incident.
 */
@Schema(
        name = "IncidentResponse",
        description = "Incident information response",
        example = """
                {
                  "id": 1,
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
public record IncidentResource(

        @Schema(description = "Incident unique identifier", example = "1")
        Long id,

        @Schema(description = "User unique identifier", example = "101")
        Long userId,

        @Schema(description = "Incident type", example = "Robbery")
        String type,

        @Schema(description = "Incident severity level", example = "HIGH")
        String severity,

        @Schema(description = "Incident address", example = "Jr. Moquegua 240")
        String address,

        @Schema(description = "Incident description", example = "Two armed suspects assaulted a pedestrian")
        String description,

        @Schema(description = "Incident current status", example = "ACTIVE")
        String status,

        @Schema(description = "Latitude coordinate", example = "-12.121")
        Double latitude,

        @Schema(description = "Longitude coordinate", example = "-77.029")
        Double longitude,

        @Schema(description = "Incident report timestamp", example = "2025-05-20T14:32:00.000Z")
        String timeReported
) {
}