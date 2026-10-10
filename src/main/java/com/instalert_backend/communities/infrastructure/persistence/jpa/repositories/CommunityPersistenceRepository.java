package com.instalert_backend.communities.infrastructure.persistence.jpa.repositories;

import com.instalert_backend.communities.infrastructure.persistence.jpa.entities.CommunityPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityPersistenceRepository extends JpaRepository<CommunityPersistenceEntity, Long> {
    boolean existsByName(String name);
}