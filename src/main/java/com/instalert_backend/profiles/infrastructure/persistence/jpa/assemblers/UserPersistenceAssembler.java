package com.instalert_backend.profiles.infrastructure.persistence.jpa.assemblers;

import com.instalert_backend.profiles.domain.model.aggregates.User;
import com.instalert_backend.profiles.infrastructure.persistence.jpa.entities.UserPersistenceEntity;

public class UserPersistenceAssembler {

    public static UserPersistenceEntity toEntity(User user) {
        UserPersistenceEntity entity = new UserPersistenceEntity();
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setRole(user.getRole());
        entity.setCurrentLocation(user.getCurrentLocation());
        entity.setAvatar(user.getAvatar());
        entity.setPhone(user.getPhone());
        entity.setBirthDate(user.getBirthDate());
        entity.setGender(user.getGender());
        return entity;
    }

    public static User toDomain(UserPersistenceEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setName(entity.getName());
        user.setEmail(entity.getEmail());
        user.setRole(entity.getRole());
        user.setCurrentLocation(entity.getCurrentLocation());
        user.setAvatar(entity.getAvatar());
        user.setPhone(entity.getPhone());
        user.setBirthDate(entity.getBirthDate());
        user.setGender(entity.getGender());
        return user;
    }
}