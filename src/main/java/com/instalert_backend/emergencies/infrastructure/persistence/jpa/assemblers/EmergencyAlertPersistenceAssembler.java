package com.instalert_backend.emergencies.infrastructure.persistence.jpa.assemblers;

import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;
import com.instalert_backend.emergencies.infrastructure.persistence.jpa.entities.EmergencyAlertPersistenceEntity;

public final class EmergencyAlertPersistenceAssembler {

    private EmergencyAlertPersistenceAssembler() {
    }

    public static EmergencyAlert toDomainFromPersistence(EmergencyAlertPersistenceEntity entity) {
        return new EmergencyAlert(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getLocation(),
                entity.getTime(),
                entity.getStatus(),
                entity.getStatusClass()
        );
    }

    public static EmergencyAlertPersistenceEntity toPersistenceFromDomain(EmergencyAlert alert) {
        var entity = new EmergencyAlertPersistenceEntity();
        entity.setId(alert.getId());
        entity.setUserId(alert.getUserId());
        entity.setType(alert.getType());
        entity.setLocation(alert.getLocation());
        entity.setTime(alert.getTime());
        entity.setStatus(alert.getStatus());
        entity.setStatusClass(alert.getStatusClass());
        return entity;
    }
}