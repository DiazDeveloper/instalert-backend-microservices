package com.instalert_backend.shared.interfaces.rest.resources;

/**
 * Resource used for simple success or information REST response
 * @param message
 */
public record MessageResource(String message) {
}