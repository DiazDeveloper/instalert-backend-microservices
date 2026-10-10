package com.instalert_backend.emergencies.interfaces.rest.transform;

import com.instalert_backend.emergencies.domain.model.commands.CreateEmergencyAlertCommand;
import com.instalert_backend.emergencies.interfaces.rest.resources.CreateEmergencyAlertResource;

public class CreateEmergencyAlertCommandFromResourceAssembler {
    public static CreateEmergencyAlertCommand toCommandFromResource(CreateEmergencyAlertResource resource) {
        return new CreateEmergencyAlertCommand(
                resource.userId(),
                resource.type(),
                resource.location(),
                resource.time(),
                resource.status(),
                resource.statusClass()
        );
    }
}