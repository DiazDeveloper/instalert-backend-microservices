package com.instalert_backend.profiles.interfaces.rest.transform;

import com.instalert_backend.profiles.domain.model.commands.CreateUserCommand;
import com.instalert_backend.profiles.interfaces.rest.resources.CreateUserResource;

public class CreateUserCommandFromResourceAssembler {

    public static CreateUserCommand toCommandFromResource(CreateUserResource resource) {
        return new CreateUserCommand(
                resource.id(),
                resource.name(),
                resource.email(),
                resource.role(),
                resource.currentLocation(),
                resource.avatar(),
                resource.phone(),
                resource.birthDate(),
                resource.gender()
        );
    }
}