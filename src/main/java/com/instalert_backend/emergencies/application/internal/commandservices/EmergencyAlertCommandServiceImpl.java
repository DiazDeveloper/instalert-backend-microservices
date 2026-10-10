package com.instalert_backend.emergencies.application.internal.commandservices;

import com.instalert_backend.emergencies.application.commandservices.EmergencyAlertCommandService;
import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;
import com.instalert_backend.emergencies.domain.model.commands.CreateEmergencyAlertCommand;
import com.instalert_backend.emergencies.domain.model.commands.UpdateEmergencyStatusCommand;
import com.instalert_backend.emergencies.domain.repositories.EmergencyAlertRepository;
import com.instalert_backend.shared.application.result.ApplicationError;
import com.instalert_backend.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class EmergencyAlertCommandServiceImpl implements EmergencyAlertCommandService {

    private final EmergencyAlertRepository emergencyAlertRepository;

    public EmergencyAlertCommandServiceImpl(EmergencyAlertRepository emergencyAlertRepository) {
        this.emergencyAlertRepository = emergencyAlertRepository;
    }

    @Override
    public Result<EmergencyAlert, ApplicationError> handle(CreateEmergencyAlertCommand command) {
        try {
            var emergencyAlert = new EmergencyAlert(command);
            var savedEmergencyAlert = emergencyAlertRepository.save(emergencyAlert);
            return Result.success(savedEmergencyAlert);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("EmergencyAlert", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("EmergencyAlert creation", e.getMessage()));
        }
    }

    @Override
    public Result<EmergencyAlert, ApplicationError> handle(UpdateEmergencyStatusCommand command) {
        try {
            var emergencyAlertOptional = emergencyAlertRepository.findById(command.alertId());
            if (emergencyAlertOptional.isEmpty()) {
                return Result.failure(ApplicationError.notFound("EmergencyAlert", command.alertId().toString()));
            }

            var emergencyAlert = emergencyAlertOptional.get();
            emergencyAlert.updateStatus(command.status(), command.statusClass());
            var savedEmergencyAlert = emergencyAlertRepository.save(emergencyAlert);
            return Result.success(savedEmergencyAlert);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("EmergencyAlert Status", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("EmergencyAlert status update", e.getMessage()));
        }
    }
}