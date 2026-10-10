package com.instalert_backend.iam.application.internal.commandservices;

import com.instalert_backend.iam.application.commandservices.RoleCommandService;
import com.instalert_backend.iam.domain.model.commands.SeedRolesCommand;
import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.iam.domain.model.valueobjects.Roles;
import com.instalert_backend.iam.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void handle(SeedRolesCommand command) {
        for (Roles roleName : Roles.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(new Role(roleName));
            }
        }
    }
}