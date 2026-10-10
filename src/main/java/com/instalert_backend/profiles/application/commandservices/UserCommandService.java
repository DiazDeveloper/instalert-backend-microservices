package com.instalert_backend.profiles.application.commandservices;

import com.instalert_backend.profiles.domain.model.aggregates.User;
import com.instalert_backend.profiles.domain.model.commands.CreateUserCommand;
import com.instalert_backend.profiles.domain.model.commands.DeleteUserCommand;
import com.instalert_backend.profiles.domain.model.commands.UpdateUserCommand;
import com.instalert_backend.profiles.domain.model.commands.UpdateUserRoleCommand;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(CreateUserCommand command);
    Optional<User> handle(UpdateUserCommand command);
    Optional<User> handle(UpdateUserRoleCommand command);
    void handle(DeleteUserCommand command);
}