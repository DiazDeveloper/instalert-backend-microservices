package com.instalert_backend.profiles.interfaces.rest.resources;

public record CreateUserResource(
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