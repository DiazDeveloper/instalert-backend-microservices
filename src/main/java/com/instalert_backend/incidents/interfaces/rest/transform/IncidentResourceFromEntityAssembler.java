package com.instalert_backend.incidents.interfaces.rest.transform;

import com.instalert_backend.incidents.domain.model.aggregates.Incident;
import com.instalert_backend.incidents.interfaces.rest.resources.IncidentResource;

public class IncidentResourceFromEntityAssembler {

    public static IncidentResource toResourceFromEntity(Incident entity) {
        return new IncidentResource(
                entity.getId(),
                entity.getUserId(),
                entity.getType(),
                entity.getSeverity(),
                entity.getAddress(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getTimeReported()
        );
    }
}