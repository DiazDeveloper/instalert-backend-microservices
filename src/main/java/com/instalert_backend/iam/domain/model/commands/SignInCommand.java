package com.instalert_backend.iam.domain.model.commands;

public record SignInCommand(String email, String password) {}