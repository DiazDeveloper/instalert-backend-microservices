package com.instalert_backend.emergencies.infrastructure.persistence.jpa.adapters;

import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;
import com.instalert_backend.emergencies.domain.repositories.EmergencyAlertRepository;
import com.instalert_backend.emergencies.infrastructure.persistence.jpa.assemblers.EmergencyAlertPersistenceAssembler;
import com.instalert_backend.emergencies.infrastructure.persistence.jpa.repositories.EmergencyAlertPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class EmergencyAlertRepositoryImpl implements EmergencyAlertRepository {

    private final EmergencyAlertPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EmergencyAlertRepositoryImpl(EmergencyAlertPersistenceRepository persistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<EmergencyAlert> findById(Long id) {
        return persistenceRepository.findById(id).map(EmergencyAlertPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<EmergencyAlert> findAll() {
        return persistenceRepository.findAll().stream()
                .map(EmergencyAlertPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public List<EmergencyAlert> findNearbyAlerts(Double lat, Double lng, Double radiusKm) {
        return Collections.emptyList();
    }

    @Override
    public EmergencyAlert save(EmergencyAlert alert) {
        boolean isNew = alert.getId() == null;
        var savedEntity = persistenceRepository.save(EmergencyAlertPersistenceAssembler.toPersistenceFromDomain(alert));
        var savedAlert = EmergencyAlertPersistenceAssembler.toDomainFromPersistence(savedEntity);

        if (isNew) {
            savedAlert.onCreated();
            savedAlert.domainEvents().forEach(eventPublisher::publishEvent);
            savedAlert.clearDomainEvents();
        }
        return savedAlert;
    }

    @Override
    public void deleteById(Long id) {
        persistenceRepository.deleteById(id);
    }
}