package com.instalert_backend.iam.application.internal.queryservices;

import com.instalert_backend.iam.application.queryservices.RoleQueryService;
import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.iam.domain.model.queries.GetAllRolesQuery;
import com.instalert_backend.iam.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleRepository roleRepository;

    public RoleQueryServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll();
    }
}