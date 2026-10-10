package com.instalert_backend.iam.application.commandservices;

import com.instalert_backend.iam.domain.model.aggregates.User;
import com.instalert_backend.iam.domain.model.commands.ChangePasswordCommand;
import com.instalert_backend.iam.domain.model.commands.SignInCommand;
import com.instalert_backend.iam.domain.model.commands.SignUpCommand;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(SignUpCommand command);
    Optional<User> handle(SignInCommand command);
    Optional<User> handle(ChangePasswordCommand command);
    String generateTokenForUser(String email);
}