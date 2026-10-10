package com.instalert_backend.profiles.domain.repositories;

import com.instalert_backend.profiles.domain.model.aggregates.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsByEmail(String email);
}