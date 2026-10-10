package com.instalert_backend.communities.domain.repositories;

import com.instalert_backend.communities.domain.model.aggregates.Community;
import java.util.List;
import java.util.Optional;

public interface CommunityRepository {
    Optional<Community> findById(Long id);
    List<Community> findAll();
    Community save(Community community);
    boolean existsByName(String name);
    void delete(Community community);
}