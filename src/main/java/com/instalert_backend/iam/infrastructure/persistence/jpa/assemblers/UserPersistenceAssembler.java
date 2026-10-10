package com.instalert_backend.iam.infrastructure.persistence.jpa.assemblers;

import com.instalert_backend.iam.domain.model.aggregates.User;
import com.instalert_backend.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;

import java.util.stream.Collectors;

public class UserPersistenceAssembler {

    public static UserPersistenceEntity toEntity(User user) {
        var entity = new UserPersistenceEntity();
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());

        var roleEntities = user.getRoles().stream()
                .map(RolePersistenceAssembler::toEntity)
                .collect(Collectors.toList());
        entity.setRoles(roleEntities);

        return entity;
    }

    public static User toDomain(UserPersistenceEntity entity) {
        var roles = entity.getRoles().stream()
                .map(RolePersistenceAssembler::toDomain)
                .collect(Collectors.toList());

        var user = new User(entity.getEmail(), entity.getPassword(), roles);
        user.setId(entity.getId());
        return user;
    }
}