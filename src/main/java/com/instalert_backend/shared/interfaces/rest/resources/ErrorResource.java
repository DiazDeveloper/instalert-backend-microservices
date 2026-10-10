package com.instalert_backend.shared.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;

/**
 * Standard error response resource returned from REST endpoint
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResource(
        String code,
        String message,
        @Nullable String details) {

    public ErrorResource(String code, String message) {
        this(code, message, null);
    }
}