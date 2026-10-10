package com.instalert_backend.iam.interfaces.rest;

import com.instalert_backend.iam.application.commandservices.UserCommandService;
import com.instalert_backend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.instalert_backend.iam.interfaces.rest.resources.SignInResource;
import com.instalert_backend.iam.interfaces.rest.resources.SignUpResource;
import com.instalert_backend.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.instalert_backend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.instalert_backend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.instalert_backend.iam.domain.model.commands.ChangePasswordCommand;
import com.instalert_backend.iam.interfaces.rest.resources.ChangePasswordResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpResource resource) {
        var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var user = userCommandService.handle(command);

        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var token = userCommandService.generateTokenForUser(user.get().getEmail());
        var authenticatedUser = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(user.get(), token);
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticatedUser);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInResource resource) {
        var command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var user = userCommandService.handle(command);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var token = userCommandService.generateTokenForUser(user.get().getEmail());
        var authenticatedUser = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(user.get(), token);
        return ResponseEntity.ok(authenticatedUser);
    }
    @PostMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordResource resource) {
        var command = new ChangePasswordCommand(userId, resource.currentPassword(), resource.newPassword());
        var user = userCommandService.handle(command);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}