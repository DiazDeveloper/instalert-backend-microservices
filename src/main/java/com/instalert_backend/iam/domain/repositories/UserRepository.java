package com.instalert_backend.iam.domain.repositories;

import com.instalert_backend.iam.domain.model.aggregates.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findById(Long id);
}