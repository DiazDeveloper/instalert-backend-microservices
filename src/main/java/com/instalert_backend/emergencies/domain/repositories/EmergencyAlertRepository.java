package com.instalert_backend.emergencies.domain.repositories;

import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;

import java.util.List;
import java.util.Optional;

public interface EmergencyAlertRepository {
    Optional<EmergencyAlert> findById(Long id);

    List<EmergencyAlert> findAll();

    List<EmergencyAlert> findNearbyAlerts(Double lat, Double lng, Double radiusKm);

    EmergencyAlert save(EmergencyAlert alert);

    void deleteById(Long id);
}