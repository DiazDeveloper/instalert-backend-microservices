package com.instalert_backend.communities.domain.model.commands;

public record CreateCommunityCommand(
        String name,
        String description,
        boolean isPrivate,
        Long ownerId
) {}