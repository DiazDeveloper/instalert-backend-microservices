package com.instalert_backend.emergencies.domain.model.events;

import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;

public record EmergencyAlertCreatedEvent(
        Long alertId,
        Long userId,
        String type,
        String location
) {
    public static EmergencyAlertCreatedEvent from(EmergencyAlert alert) {
        return new EmergencyAlertCreatedEvent(
                alert.getId(),
                alert.getUserId(),
                alert.getType(),
                alert.getLocation()
        );
    }
}