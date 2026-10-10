package com.instalert_backend.incidents.application.queryservices;

import com.instalert_backend.incidents.domain.model.aggregates.Incident;

import com.instalert_backend.incidents.domain.model.queries.GetAllIncidentsQuery;
import com.instalert_backend.incidents.domain.model.queries.GetIncidentByIdQuery;

import java.util.List;
import java.util.Optional;

public interface IncidentQueryService {

    Optional<Incident> handle(GetIncidentByIdQuery query);

    List<Incident> handle(GetAllIncidentsQuery query);
}