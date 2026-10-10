package com.instalert_backend.communities.application.internal.eventhandlers;

import com.instalert_backend.communities.domain.model.events.CommunityCreatedEvent;
import com.instalert_backend.communities.interfaces.events.CommunityCreatedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service("communitiesCommunityCreatedEventHandler")
public class CommunityCreatedEventHandler {
    private final ApplicationEventPublisher eventPublisher;

    public CommunityCreatedEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(CommunityCreatedEvent event) {
        // Cuando se crea en el dominio, lo publicamos para el resto del sistema
        eventPublisher.publishEvent(new CommunityCreatedIntegrationEvent(
                event.communityId(),
                event.name(),
                event.isPrivate()
        ));
    }
}