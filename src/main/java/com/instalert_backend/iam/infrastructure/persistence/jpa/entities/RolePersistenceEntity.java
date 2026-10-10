package com.instalert_backend.iam.infrastructure.persistence.jpa.entities;

import com.instalert_backend.iam.domain.model.valueobjects.Roles;
import com.instalert_backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class RolePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Enumerated(EnumType.STRING)
    private Roles name;

    public RolePersistenceEntity() {}

    public RolePersistenceEntity(Roles name) {
        this.name = name;
    }
}