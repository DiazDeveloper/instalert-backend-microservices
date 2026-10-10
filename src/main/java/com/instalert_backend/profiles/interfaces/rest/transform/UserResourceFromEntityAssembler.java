package com.instalert_backend.profiles.interfaces.rest.transform;

import com.instalert_backend.profiles.domain.model.aggregates.User;
import com.instalert_backend.profiles.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {

    public static UserResource toResourceFromEntity(User user) {
        return new UserResource(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCurrentLocation(),
                user.getAvatar(),
                user.getPhone(),
                user.getBirthDate(),
                user.getGender()
        );
    }
}