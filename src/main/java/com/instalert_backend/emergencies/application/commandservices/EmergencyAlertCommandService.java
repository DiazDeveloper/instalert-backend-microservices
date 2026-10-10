package com.instalert_backend.emergencies.application.commandservices;

import com.instalert_backend.emergencies.domain.model.aggregates.EmergencyAlert;
import com.instalert_backend.emergencies.domain.model.commands.CreateEmergencyAlertCommand;
import com.instalert_backend.emergencies.domain.model.commands.UpdateEmergencyStatusCommand;
import com.instalert_backend.shared.application.result.ApplicationError;
import com.instalert_backend.shared.application.result.Result;

public interface EmergencyAlertCommandService {
    Result<EmergencyAlert, ApplicationError> handle(CreateEmergencyAlertCommand command);
    Result<EmergencyAlert, ApplicationError> handle(UpdateEmergencyStatusCommand command);
}