package com.instalert_backend.communities.interfaces.rest.transform;

import com.instalert_backend.communities.domain.model.commands.CreateCommunityCommand;
import com.instalert_backend.communities.interfaces.rest.resources.CreateCommunityResource;

public class CreateCommunityCommandFromResourceAssembler {
    public static CreateCommunityCommand toCommandFromResource(CreateCommunityResource resource) {
        return new CreateCommunityCommand(
                resource.name(),
                resource.description(),
                resource.isPrivate(),
                resource.ownerId()
        );
    }
}