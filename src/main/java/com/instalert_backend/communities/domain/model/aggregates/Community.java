package com.instalert_backend.communities.domain.model.aggregates;

import com.instalert_backend.communities.domain.model.commands.CreateCommunityCommand;
import com.instalert_backend.communities.domain.model.events.CommunityCreatedEvent;
import com.instalert_backend.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;


public class Community extends AbstractDomainAggregateRoot<Community> {
    @Getter
    private Long id;

    @Getter
    private String name;

    @Getter
    private String description;

    @Getter
    private boolean isPrivate;

    @Getter
    private Long ownerId;


    public Community() {
    }


    public Community(CreateCommunityCommand command) {
        this.name = command.name();
        this.description = command.description();
        this.isPrivate = command.isPrivate();
        this.ownerId = command.ownerId();
    }


    public Community(Long id, String name, String description, boolean isPrivate, Long ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
        this.ownerId = ownerId;
    }

    public void onCreated() {
        registerDomainEvent(new CommunityCreatedEvent(this.id, this.name, this.isPrivate));
    }
}