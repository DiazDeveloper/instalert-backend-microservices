package com.instalert_backend.iam.application.commandservices;

import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}