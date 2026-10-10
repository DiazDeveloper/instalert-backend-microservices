package com.instalert_backend.emergencies.domain.model.commands;

public record UpdateEmergencyStatusCommand(
        Long alertId,
        String status,
        String statusClass
) {
    public UpdateEmergencyStatusCommand {
        if (alertId == null || alertId <= 0) throw new IllegalArgumentException("El alertId es inválido");
        if (status == null || status.isBlank()) throw new IllegalArgumentException("El status es requerido");
    }
}