package com.instalert_backend.profiles.infrastructure.persistence.jpa.repositories;

import com.instalert_backend.profiles.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPersistenceRepository extends JpaRepository<UserPersistenceEntity, Long> {
    boolean existsByEmail(String email);
}