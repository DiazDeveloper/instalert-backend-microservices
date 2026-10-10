package com.instalert_backend.emergencies.interfaces.rest.transform;

import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;
import com.instalert_backend.emergencies.interfaces.rest.resources.EmergencyAlertResource;

public class EmergencyAlertResourceFromEntityAssembler {
    public static EmergencyAlertResource toResourceFromEntity(EmergencyAlert entity) {
        return new EmergencyAlertResource(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getLocation(),
                entity.getTime(),
                entity.getStatus(),
                entity.getStatusClass()
        );
    }
}