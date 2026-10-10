package com.instalert_backend.incidents.domain.model.commands;

public record CreateIncidentCommand(
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
}