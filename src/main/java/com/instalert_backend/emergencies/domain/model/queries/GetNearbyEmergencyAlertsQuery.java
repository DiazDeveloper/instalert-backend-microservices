package com.instalert_backend.emergencies.domain.model.queries;

public record GetNearbyEmergencyAlertsQuery(Double latitude, Double longitude, Double radiusKm) {
    public GetNearbyEmergencyAlertsQuery {
        if (latitude == null || longitude == null) throw new IllegalArgumentException("Las coordenadas son requeridas");
        if (radiusKm == null || radiusKm <= 0) throw new IllegalArgumentException("El radio debe ser mayor a 0");
    }
}