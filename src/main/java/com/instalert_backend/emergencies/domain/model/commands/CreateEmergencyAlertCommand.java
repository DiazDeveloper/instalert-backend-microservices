package com.instalert_backend.emergencies.domain.model.commands;

public record CreateEmergencyAlertCommand(
        Long userId,
        String type,
        String location,
        String time,
        String status,
        String statusClass
) {
    public CreateEmergencyAlertCommand {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("El userId es inválido");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("El tipo es requerido");
        if (location == null || location.isBlank()) throw new IllegalArgumentException("La ubicación es requerida");
    }
}