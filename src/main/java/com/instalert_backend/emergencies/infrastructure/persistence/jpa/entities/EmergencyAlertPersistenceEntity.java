package com.instalert_backend.emergencies.infrastructure.persistence.jpa.entities;

import com.instalert_backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "emergency_alerts")
public class EmergencyAlertPersistenceEntity extends AuditableAbstractPersistenceEntity {
    private Long userId;
    private String type;
    private String location;
    private String time;
    private String status;
    private String statusClass;

    public EmergencyAlertPersistenceEntity() {
    }
}