package com.instalert_backend.emergencies.infrastructure.persistence.jpa.repositories;

import com.instalert_backend.emergencies.infrastructure.persistence.jpa.entities.EmergencyAlertPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyAlertPersistenceRepository extends JpaRepository<EmergencyAlertPersistenceEntity, Long> {
}