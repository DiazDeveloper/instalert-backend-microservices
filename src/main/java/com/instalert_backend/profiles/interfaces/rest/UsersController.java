package com.instalert_backend.profiles.interfaces.rest;

import com.instalert_backend.profiles.application.commandservices.UserCommandService;
import com.instalert_backend.profiles.application.queryservices.UserQueryService;
import com.instalert_backend.profiles.domain.model.commands.DeleteUserCommand;
import com.instalert_backend.profiles.domain.model.queries.GetAllUsersQuery;
import com.instalert_backend.profiles.domain.model.queries.GetUserByIdQuery;
import com.instalert_backend.profiles.interfaces.rest.resources.CreateUserResource;
import com.instalert_backend.profiles.interfaces.rest.resources.UpdateUserResource;
import com.instalert_backend.profiles.interfaces.rest.resources.UserResource;
import com.instalert_backend.profiles.interfaces.rest.transform.CreateUserCommandFromResourceAssembler;
import com.instalert_backend.profiles.interfaces.rest.transform.UpdateUserCommandFromResourceAssembler;
import com.instalert_backend.profiles.interfaces.rest.transform.UserResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.instalert_backend.profiles.interfaces.rest.resources.UpdateUserRoleResource;
import com.instalert_backend.profiles.interfaces.rest.transform.UpdateUserRoleCommandFromResourceAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @PostMapping
    public ResponseEntity<UserResource> createUser(@RequestBody CreateUserResource resource) {
        var command = CreateUserCommandFromResourceAssembler.toCommandFromResource(resource);
        var user = userCommandService.handle(command);
        return user.map(u -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(UserResourceFromEntityAssembler.toResourceFromEntity(u)))
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var query = new GetAllUsersQuery();
        var users = userQueryService.handle(query);
        var resources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long id) {
        var query = new GetUserByIdQuery(id);
        var user = userQueryService.handle(query);
        return user.map(u -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResource> updateUser(@PathVariable Long id, @RequestBody UpdateUserResource resource) {
        var command = UpdateUserCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var user = userCommandService.handle(command);
        return user.map(u -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResource> updateUserRole(@PathVariable Long id, @RequestBody UpdateUserRoleResource resource) {
        var command = UpdateUserRoleCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var user = userCommandService.handle(command);
        return user.map(u -> ResponseEntity.ok(UserResourceFromEntityAssembler.toResourceFromEntity(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userCommandService.handle(new DeleteUserCommand(id));
        return ResponseEntity.noContent().build();
    }
}