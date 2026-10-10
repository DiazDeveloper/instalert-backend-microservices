package com.instalert_backend.incidents.domain.model.events;

import com.instalert_backend.incidents.domain.model.aggregates.Incident;

public record IncidentCreatedEvent(
        Long incidentId,
        Long userId,
        String type,
        String severity,
        String address,
        String description,
        String status,
        Double latitude,
        Double longitude,
        String timeReported
) {
    public static IncidentCreatedEvent from(Incident incident) {
        return new IncidentCreatedEvent(
                incident.getId(),
                incident.getUserId(),
                incident.getType(),
                incident.getSeverity(),
                incident.getAddress(),
                incident.getDescription(),
                incident.getStatus(),
                incident.getLatitude(),
                incident.getLongitude(),
                incident.getTimeReported()
        );
    }
}