package com.instalert_backend.profiles.interfaces.rest.transform;

import com.instalert_backend.profiles.domain.model.commands.UpdateUserCommand;
import com.instalert_backend.profiles.interfaces.rest.resources.UpdateUserResource;

public class UpdateUserCommandFromResourceAssembler {
    public static UpdateUserCommand toCommandFromResource(Long id, UpdateUserResource resource) {
        return new UpdateUserCommand(id, resource.email(), resource.phone(), resource.birthDate(), resource.gender());
    }
}