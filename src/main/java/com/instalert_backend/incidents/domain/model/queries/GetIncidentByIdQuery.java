package com.instalert_backend.incidents.domain.model.queries;

import com.instalert_backend.incidents.domain.model.queries.GetIncidentByIdQuery;

import org.springframework.web.bind.annotation.PathVariable;

public record GetIncidentByIdQuery(Long incidentId) {
}