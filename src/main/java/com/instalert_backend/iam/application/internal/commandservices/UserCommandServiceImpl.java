package com.instalert_backend.iam.application.internal.commandservices;

import com.instalert_backend.iam.application.commandservices.UserCommandService;
import com.instalert_backend.iam.application.internal.outboundservices.hashing.HashingService;
import com.instalert_backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.instalert_backend.iam.domain.model.aggregates.User;
import com.instalert_backend.iam.domain.model.commands.SignInCommand;
import com.instalert_backend.iam.domain.model.commands.SignUpCommand;
import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.iam.domain.model.valueobjects.Roles;
import com.instalert_backend.iam.domain.repositories.RoleRepository;
import com.instalert_backend.iam.domain.repositories.UserRepository;
import com.instalert_backend.iam.domain.model.commands.ChangePasswordCommand;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  HashingService hashingService,
                                  TokenService tokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        List<Role> roles = new ArrayList<>();
        if (command.roles() == null || command.roles().isEmpty()) {
            var defaultRole = roleRepository.findByName(Roles.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Rol por defecto no encontrado"));
            roles.add(defaultRole);
        } else {
            for (String roleName : command.roles()) {
                var role = roleRepository.findByName(Roles.valueOf(roleName))
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
                roles.add(role);
            }
        }

        var encodedPassword = hashingService.encode(command.password());
        var user = new User(command.email(), encodedPassword, roles);
        var savedUser = userRepository.save(user);
        return Optional.of(savedUser);
    }

    @Override
    public Optional<User> handle(SignInCommand command) {
        var userOpt = userRepository.findByEmail(command.email());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        var user = userOpt.get();
        if (!hashingService.matches(command.password(), user.getPassword())) {
            return Optional.empty();
        }

        return Optional.of(user);
    }
    @Override
    public Optional<User> handle(ChangePasswordCommand command) {
        var userOpt = userRepository.findById(command.userId());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        var user = userOpt.get();
        if (!hashingService.matches(command.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        user.setPassword(hashingService.encode(command.newPassword()));
        var updatedUser = userRepository.save(user);
        return Optional.of(updatedUser);
    }

    @Override
    public String generateTokenForUser(String email) {
        return tokenService.generateToken(email);
    }
}