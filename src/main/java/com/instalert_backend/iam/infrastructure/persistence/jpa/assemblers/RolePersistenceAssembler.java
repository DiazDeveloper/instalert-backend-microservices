package com.instalert_backend.iam.infrastructure.persistence.jpa.assemblers;

import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;

public class RolePersistenceAssembler {

    public static RolePersistenceEntity toEntity(Role role) {
        var entity = new RolePersistenceEntity(role.getName());
        if (role.getId() != null) {
            entity.setId(role.getId());
        }
        return entity;
    }

    public static Role toDomain(RolePersistenceEntity entity) {
        var role = new Role(entity.getName());
        role.setId(entity.getId());
        return role;
    }
}