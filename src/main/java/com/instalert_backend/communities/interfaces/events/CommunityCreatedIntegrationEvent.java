package com.instalert_backend.communities.interfaces.events;

import com.instalert_backend.communities.domain.model.aggregates.Community;

public record CommunityCreatedIntegrationEvent(Long communityId, String name, boolean isPrivate) {

    public static CommunityCreatedIntegrationEvent from(Community community) {
        return new CommunityCreatedIntegrationEvent(
                community.getId(),
                community.getName(),
                community.isPrivate()
        );
    }
}