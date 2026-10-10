package com.instalert_backend.profiles.domain.model.commands;

public record CreateUserCommand(
        Long id,
        String name,
        String email,
        String role,
        String currentLocation,
        String avatar,
        String phone,
        String birthDate,
        String gender
) {}