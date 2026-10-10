package com.instalert_backend.iam.infrastructure.persistence.jpa.repositories;

import com.instalert_backend.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("iamUserPersistenceRepository")
public interface IamUserPersistenceRepository extends JpaRepository<UserPersistenceEntity, Long> {
    Optional<UserPersistenceEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}