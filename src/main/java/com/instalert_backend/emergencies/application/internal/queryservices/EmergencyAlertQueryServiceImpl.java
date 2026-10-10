package com.instalert_backend.emergencies.application.internal.queryservices;

import com.instalert_backend.emergencies.application.queryservices.EmergencyAlertQueryService;
import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;
import com.instalert_backend.emergencies.domain.model.queries.GetAllEmergencyAlertsQuery;
import com.instalert_backend.emergencies.domain.model.queries.GetEmergencyAlertByIdQuery;
import com.instalert_backend.emergencies.domain.model.queries.GetNearbyEmergencyAlertsQuery;
import com.instalert_backend.emergencies.domain.repositories.EmergencyAlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmergencyAlertQueryServiceImpl implements EmergencyAlertQueryService {

    private final EmergencyAlertRepository emergencyAlertRepository;

    public EmergencyAlertQueryServiceImpl(EmergencyAlertRepository emergencyAlertRepository) {
        this.emergencyAlertRepository = emergencyAlertRepository;
    }

    @Override
    public Optional<EmergencyAlert> handle(GetEmergencyAlertByIdQuery query) {
        return emergencyAlertRepository.findById(query.alertId());
    }

    @Override
    public List<EmergencyAlert> handle(GetAllEmergencyAlertsQuery query) {
        return emergencyAlertRepository.findAll();
    }

    @Override
    public List<EmergencyAlert> handle(GetNearbyEmergencyAlertsQuery query) {
        return emergencyAlertRepository.findNearbyAlerts(query.latitude(), query.longitude(), query.radiusKm());
    }
}