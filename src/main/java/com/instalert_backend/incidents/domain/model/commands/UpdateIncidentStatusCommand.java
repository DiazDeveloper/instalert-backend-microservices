package com.instalert_backend.incidents.domain.model.commands;

public record UpdateIncidentStatusCommand(Long id, String status) {}