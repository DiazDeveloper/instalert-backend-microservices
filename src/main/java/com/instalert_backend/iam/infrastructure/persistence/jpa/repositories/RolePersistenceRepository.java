package com.instalert_backend.iam.infrastructure.persistence.jpa.repositories;

import com.instalert_backend.iam.domain.model.valueobjects.Roles;
import com.instalert_backend.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolePersistenceRepository extends JpaRepository<RolePersistenceEntity, Long> {
    Optional<RolePersistenceEntity> findByName(Roles name);
    boolean existsByName(Roles name);
}