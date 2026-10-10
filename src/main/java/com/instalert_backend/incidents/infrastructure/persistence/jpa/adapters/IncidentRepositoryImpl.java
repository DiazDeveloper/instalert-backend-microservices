package com.instalert_backend.incidents.infrastructure.persistence.jpa.adapters;

import com.instalert_backend.incidents.domain.model.aggregates.Incident;
import com.instalert_backend.incidents.domain.repositories.IncidentRepository;
import com.instalert_backend.incidents.infrastructure.persistence.jpa.assemblers.IncidentPersistenceAssembler;
import com.instalert_backend.incidents.infrastructure.persistence.jpa.repositories.IncidentPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class IncidentRepositoryImpl implements IncidentRepository {

    private final IncidentPersistenceRepository incidentPersistenceRepository;

    public IncidentRepositoryImpl(IncidentPersistenceRepository incidentPersistenceRepository) {
        this.incidentPersistenceRepository = incidentPersistenceRepository;
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return incidentPersistenceRepository.findById(id).map(IncidentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Incident> findAll() {
        return incidentPersistenceRepository.findAll().stream().map(IncidentPersistenceAssembler::toDomainFromPersistence).toList();
    }

    @Override
    public Incident save(Incident incident) {
        var savedEntity = incidentPersistenceRepository.save(IncidentPersistenceAssembler.toPersistenceFromDomain(incident));
        return IncidentPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        incidentPersistenceRepository.deleteById(id);
    }
}