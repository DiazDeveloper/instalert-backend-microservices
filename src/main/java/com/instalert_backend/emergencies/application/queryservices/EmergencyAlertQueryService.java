package com.instalert_backend.emergencies.application.queryservices;

import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;
import com.instalert_backend.emergencies.domain.model.queries.GetAllEmergencyAlertsQuery;
import com.instalert_backend.emergencies.domain.model.queries.GetEmergencyAlertByIdQuery;
import com.instalert_backend.emergencies.domain.model.queries.GetNearbyEmergencyAlertsQuery;

import java.util.List;
import java.util.Optional;

public interface EmergencyAlertQueryService {
    Optional<EmergencyAlert> handle(GetEmergencyAlertByIdQuery query);
    List<EmergencyAlert> handle(GetAllEmergencyAlertsQuery query);
    List<EmergencyAlert> handle(GetNearbyEmergencyAlertsQuery query);
}