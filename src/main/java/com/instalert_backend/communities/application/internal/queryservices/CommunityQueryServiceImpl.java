package com.instalert_backend.communities.application.internal.queryservices;

import com.instalert_backend.communities.application.queryservices.CommunityQueryService;
import com.instalert_backend.communities.domain.model.aggregates.Community;
import com.instalert_backend.communities.domain.model.queries.GetAllCommunitiesQuery;
import com.instalert_backend.communities.domain.model.queries.GetCommunityByIdQuery;
import com.instalert_backend.communities.domain.repositories.CommunityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommunityQueryServiceImpl implements CommunityQueryService {
    private final CommunityRepository communityRepository;

    public CommunityQueryServiceImpl(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    @Override
    public Optional<Community> handle(GetCommunityByIdQuery query) {
        return communityRepository.findById(query.communityId());
    }

    @Override
    public List<Community> handle(GetAllCommunitiesQuery query) {
        return communityRepository.findAll();
    }
}