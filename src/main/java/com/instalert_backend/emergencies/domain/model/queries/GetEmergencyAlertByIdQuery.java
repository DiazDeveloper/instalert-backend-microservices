package com.instalert_backend.emergencies.domain.model.queries;

public record GetEmergencyAlertByIdQuery(Long alertId) {
    public GetEmergencyAlertByIdQuery {
        if (alertId == null || alertId <= 0) throw new IllegalArgumentException("El alertId es requerido y mayor a 0");
    }
}