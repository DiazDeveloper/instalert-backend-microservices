package com.instalert_backend.iam.interfaces.rest.resources;

public record ChangePasswordResource(String currentPassword, String newPassword) {}