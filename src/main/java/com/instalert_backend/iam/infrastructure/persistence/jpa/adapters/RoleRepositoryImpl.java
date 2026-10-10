package com.instalert_backend.iam.infrastructure.persistence.jpa.adapters;

import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.iam.domain.model.valueobjects.Roles;
import com.instalert_backend.iam.domain.repositories.RoleRepository;
import com.instalert_backend.iam.infrastructure.persistence.jpa.assemblers.RolePersistenceAssembler;
import com.instalert_backend.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final RolePersistenceRepository rolePersistenceRepository;

    public RoleRepositoryImpl(RolePersistenceRepository rolePersistenceRepository) {
        this.rolePersistenceRepository = rolePersistenceRepository;
    }

    @Override
    public Role save(Role role) {
        var entity = RolePersistenceAssembler.toEntity(role);
        var saved = rolePersistenceRepository.save(entity);
        return RolePersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Role> findByName(Roles name) {
        return rolePersistenceRepository.findByName(name)
                .map(RolePersistenceAssembler::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return rolePersistenceRepository.findAll().stream()
                .map(RolePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(Roles name) {
        return rolePersistenceRepository.existsByName(name);
    }
}