package com.instalert_backend.incidents.domain.repositories;

import com.instalert_backend.incidents.domain.model.aggregates.Incident;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository {

    Optional<Incident> findById(Long id);

    List<Incident> findAll();

    Incident save(Incident incident);

    void deleteById(Long id);
}