package com.instalert_backend.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(Long id, String email, String token) {}