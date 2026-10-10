package com.instalert_backend.iam.infrastructure.persistence.jpa.adapters;

import com.instalert_backend.iam.domain.model.aggregates.User;
import com.instalert_backend.iam.domain.repositories.UserRepository;
import com.instalert_backend.iam.infrastructure.persistence.jpa.assemblers.UserPersistenceAssembler;
import com.instalert_backend.iam.infrastructure.persistence.jpa.repositories.IamUserPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("iamUserRepository")
public class UserRepositoryImpl implements UserRepository {

    private final IamUserPersistenceRepository userPersistenceRepository;

    public UserRepositoryImpl(IamUserPersistenceRepository userPersistenceRepository) {
        this.userPersistenceRepository = userPersistenceRepository;
    }

    @Override
    public User save(User user) {
        var entity = UserPersistenceAssembler.toEntity(user);
        var saved = userPersistenceRepository.save(entity);
        return UserPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userPersistenceRepository.findByEmail(email)
                .map(UserPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userPersistenceRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userPersistenceRepository.findById(id)
                .map(UserPersistenceAssembler::toDomain);
    }
}