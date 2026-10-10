package com.instalert_backend.communities.infrastructure.persistence.jpa.adapters;

import com.instalert_backend.communities.domain.model.aggregates.Community;
import com.instalert_backend.communities.domain.repositories.CommunityRepository;
import com.instalert_backend.communities.infrastructure.persistence.jpa.assemblers.CommunityPersistenceAssembler;
import com.instalert_backend.communities.infrastructure.persistence.jpa.repositories.CommunityPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommunityRepositoryImpl implements CommunityRepository {

    private final CommunityPersistenceRepository communityPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommunityRepositoryImpl(CommunityPersistenceRepository communityPersistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.communityPersistenceRepository = communityPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Community> findById(Long id) {
        return communityPersistenceRepository.findById(id).map(CommunityPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Community> findAll() {
        return communityPersistenceRepository.findAll().stream().map(CommunityPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public Community save(Community community) {
        boolean isNew = community.getId() == null;
        var savedEntity = communityPersistenceRepository.save(CommunityPersistenceAssembler.toPersistenceFromDomain(community));
        var savedCommunity = CommunityPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedCommunity.onCreated();
            savedCommunity.domainEvents().forEach(eventPublisher::publishEvent);
            savedCommunity.clearDomainEvents();
        }
        return savedCommunity;
    }

    @Override
    public boolean existsByName(String name) {
        return communityPersistenceRepository.existsByName(name);
    }

    @Override
    public void delete(Community community) {
        communityPersistenceRepository.deleteById(community.getId());
    }
}