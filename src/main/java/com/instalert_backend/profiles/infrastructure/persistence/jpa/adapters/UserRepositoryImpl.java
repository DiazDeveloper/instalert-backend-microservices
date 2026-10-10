package com.instalert_backend.profiles.infrastructure.persistence.jpa.adapters;

import com.instalert_backend.profiles.domain.model.aggregates.User;
import com.instalert_backend.profiles.domain.repositories.UserRepository;
import com.instalert_backend.profiles.infrastructure.persistence.jpa.assemblers.UserPersistenceAssembler;
import com.instalert_backend.profiles.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.instalert_backend.profiles.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("profilesUserRepository")
public class UserRepositoryImpl implements UserRepository {

    private final UserPersistenceRepository userPersistenceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public UserRepositoryImpl(UserPersistenceRepository userPersistenceRepository) {
        this.userPersistenceRepository = userPersistenceRepository;
    }

    @Override
    @Transactional
    public User save(User user) {
        boolean exists = user.getId() != null && userPersistenceRepository.existsById(user.getId());

        if (exists) {
            var entity = UserPersistenceAssembler.toEntity(user);
            var merged = entityManager.merge(entity);
            return UserPersistenceAssembler.toDomain(merged);
        }

        if (user.getId() != null) {
            entityManager.createNativeQuery(
                            "INSERT INTO users (id, created_at, updated_at, name, email, role, current_location, avatar, phone, birth_date, gender) " +
                                    "VALUES (?, NOW(), NOW(), ?, ?, ?, ?, ?, ?, ?, ?)"
                    )
                    .setParameter(1, user.getId())
                    .setParameter(2, user.getName())
                    .setParameter(3, user.getEmail())
                    .setParameter(4, user.getRole())
                    .setParameter(5, user.getCurrentLocation())
                    .setParameter(6, user.getAvatar())
                    .setParameter(7, user.getPhone())
                    .setParameter(8, user.getBirthDate())
                    .setParameter(9, user.getGender())
                    .executeUpdate();

            entityManager.flush();
            entityManager.clear();

            var saved = userPersistenceRepository.findById(user.getId()).orElseThrow();
            return UserPersistenceAssembler.toDomain(saved);
        }

        var entity = UserPersistenceAssembler.toEntity(user);
        var saved = userPersistenceRepository.save(entity);
        return UserPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userPersistenceRepository.findById(id)
                .map(UserPersistenceAssembler::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userPersistenceRepository.findAll()
                .stream()
                .map(UserPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        userPersistenceRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userPersistenceRepository.existsByEmail(email);
    }
}