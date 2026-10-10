package com.instalert_backend.communities.application.queryservices;

import com.instalert_backend.communities.domain.model.aggregates.Community;
import com.instalert_backend.communities.domain.model.queries.GetAllCommunitiesQuery;
import com.instalert_backend.communities.domain.model.queries.GetCommunityByIdQuery;

import java.util.List;
import java.util.Optional;

public interface CommunityQueryService {
    Optional<Community> handle(GetCommunityByIdQuery query);
    List<Community> handle(GetAllCommunitiesQuery query);
}