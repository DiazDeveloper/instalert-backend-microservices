package com.instalert_backend.profiles.domain.model.commands;

public record UpdateUserCommand(Long id, String email, String phone, String birthDate, String gender) {}