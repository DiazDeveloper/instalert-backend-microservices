package com.instalert_backend.communities.infrastructure.persistence.jpa.assemblers;

import com.instalert_backend.communities.domain.model.aggregates.Community;
import com.instalert_backend.communities.infrastructure.persistence.jpa.entities.CommunityPersistenceEntity;

public final class CommunityPersistenceAssembler {
    private CommunityPersistenceAssembler() {}


    public static Community toDomainFromPersistence(CommunityPersistenceEntity entity) {
        return new Community(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isPrivate(),
                entity.getOwnerId()
        );
    }


    public static CommunityPersistenceEntity toPersistenceFromDomain(Community community) {
        var entity = new CommunityPersistenceEntity();
        entity.setId(community.getId());
        entity.setName(community.getName());
        entity.setDescription(community.getDescription());
        entity.setPrivate(community.isPrivate());
        entity.setOwnerId(community.getOwnerId());
        return entity;
    }
}