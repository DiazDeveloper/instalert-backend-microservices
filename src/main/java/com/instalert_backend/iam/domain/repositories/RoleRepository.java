package com.instalert_backend.iam.domain.repositories;

import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.iam.domain.model.valueobjects.Roles;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Role save(Role role);
    Optional<Role> findByName(Roles name);
    List<Role> findAll();
    boolean existsByName(Roles name);
}