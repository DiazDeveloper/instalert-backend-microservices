package com.instalert_backend.incidents.application.internal.queryservices;

import com.instalert_backend.incidents.application.queryservices.IncidentQueryService;
import com.instalert_backend.incidents.domain.model.aggregates.Incident;
import com.instalert_backend.incidents.domain.model.queries.GetAllIncidentsQuery;
import com.instalert_backend.incidents.domain.model.queries.GetIncidentByIdQuery;
import com.instalert_backend.incidents.domain.repositories.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentQueryServiceImpl implements IncidentQueryService {

    private final IncidentRepository incidentRepository;

    public IncidentQueryServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public Optional<Incident> handle(GetIncidentByIdQuery query) {
        return incidentRepository.findById(query.incidentId());
    }

    @Override
    public List<Incident> handle(GetAllIncidentsQuery query) {
        return incidentRepository.findAll();
    }
}