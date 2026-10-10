package com.instalert_backend.profiles.application.internal.commandservices;

import com.instalert_backend.profiles.application.commandservices.UserCommandService;
import com.instalert_backend.profiles.domain.model.aggregates.User;
import com.instalert_backend.profiles.domain.model.commands.CreateUserCommand;
import com.instalert_backend.profiles.domain.model.commands.DeleteUserCommand;
import com.instalert_backend.profiles.domain.model.commands.UpdateUserCommand;
import com.instalert_backend.profiles.domain.model.commands.UpdateUserRoleCommand;
import com.instalert_backend.profiles.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("profilesUserCommandService")
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;

    public UserCommandServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        var user = new User(command);
        var savedUser = userRepository.save(user);
        return Optional.of(savedUser);
    }

    @Override
    public Optional<User> handle(UpdateUserCommand command) {
        var existingUser = userRepository.findById(command.id());
        if (existingUser.isEmpty()) {
            return Optional.empty();
        }
        var user = existingUser.get();
        user.setEmail(command.email());
        user.setPhone(command.phone());
        user.setBirthDate(command.birthDate());
        user.setGender(command.gender());
        var updatedUser = userRepository.save(user);
        return Optional.of(updatedUser);
    }

    @Override
    public Optional<User> handle(UpdateUserRoleCommand command) {
        var existingUser = userRepository.findById(command.id());
        if (existingUser.isEmpty()) {
            return Optional.empty();
        }
        var user = existingUser.get();
        user.setRole(command.role());
        var updatedUser = userRepository.save(user);
        return Optional.of(updatedUser);
    }

    @Override
    public void handle(DeleteUserCommand command) {
        userRepository.deleteById(command.id());
    }
}