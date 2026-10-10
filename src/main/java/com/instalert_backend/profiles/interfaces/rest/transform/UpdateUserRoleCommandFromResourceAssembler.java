package com.instalert_backend.profiles.interfaces.rest.transform;

import com.instalert_backend.profiles.domain.model.commands.UpdateUserRoleCommand;
import com.instalert_backend.profiles.interfaces.rest.resources.UpdateUserRoleResource;

public class UpdateUserRoleCommandFromResourceAssembler {
    public static UpdateUserRoleCommand toCommandFromResource(Long id, UpdateUserRoleResource resource) {
        return new UpdateUserRoleCommand(id, resource.role());
    }
}