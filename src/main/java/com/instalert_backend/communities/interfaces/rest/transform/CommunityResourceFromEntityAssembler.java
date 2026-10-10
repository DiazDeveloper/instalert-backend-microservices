package com.instalert_backend.communities.interfaces.rest.transform;

import com.instalert_backend.communities.domain.model.aggregates.Community;
import com.instalert_backend.communities.interfaces.rest.resources.CommunityResource;

public class CommunityResourceFromEntityAssembler {
    public static CommunityResource toResourceFromEntity(Community entity) {
        return new CommunityResource(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isPrivate(),
                entity.getOwnerId()
        );
    }
}