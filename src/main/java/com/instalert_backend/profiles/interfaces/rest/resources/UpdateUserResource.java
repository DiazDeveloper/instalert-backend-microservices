package com.instalert_backend.profiles.interfaces.rest.resources;

public record UpdateUserResource(String email, String phone, String birthDate, String gender) {}