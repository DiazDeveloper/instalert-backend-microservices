package com.instalert_backend.incidents.infrastructure.persistence.jpa.assemblers;

import com.instalert_backend.incidents.domain.model.aggregates.Incident;
import com.instalert_backend.incidents.infrastructure.persistence.jpa.entities.IncidentPersistenceEntity;

public final class IncidentPersistenceAssembler {

    private IncidentPersistenceAssembler() {
    }

    public static Incident toDomainFromPersistence(IncidentPersistenceEntity entity) {
        return new Incident(
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

    public static IncidentPersistenceEntity toPersistenceFromDomain(Incident incident) {
        var entity = new IncidentPersistenceEntity();
        entity.setId(incident.getId());
        entity.setUserId(incident.getUserId());
        entity.setType(incident.getType());
        entity.setSeverity(incident.getSeverity());
        entity.setAddress(incident.getAddress());
        entity.setDescription(incident.getDescription());
        entity.setStatus(incident.getStatus());
        entity.setLatitude(incident.getLatitude());
        entity.setLongitude(incident.getLongitude());
        entity.setTimeReported(incident.getTimeReported());
        return entity;
    }
}