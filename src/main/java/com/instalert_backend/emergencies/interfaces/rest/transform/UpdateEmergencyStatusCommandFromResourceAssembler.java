package com.instalert_backend.emergencies.interfaces.rest.transform;

import com.instalert_backend.emergencies.domain.model.commands.UpdateEmergencyStatusCommand;
import com.instalert_backend.emergencies.interfaces.rest.resources.UpdateEmergencyStatusResource;

public class UpdateEmergencyStatusCommandFromResourceAssembler {
    public static UpdateEmergencyStatusCommand toCommandFromResource(Long alertId, UpdateEmergencyStatusResource resource) {
        return new UpdateEmergencyStatusCommand(
                alertId,
                resource.status(),
                resource.statusClass()
        );
    }
}