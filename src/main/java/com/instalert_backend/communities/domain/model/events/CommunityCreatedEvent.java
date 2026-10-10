package com.instalert_backend.communities.domain.model.events;

import com.instalert_backend.communities.domain.model.aggregates.Community;

public record CommunityCreatedEvent(Long communityId, String name, boolean isPrivate) {

    public static CommunityCreatedEvent from(Community community) {
        return new CommunityCreatedEvent(
                community.getId(),
                community.getName(),
                community.isPrivate()
        );
    }
}