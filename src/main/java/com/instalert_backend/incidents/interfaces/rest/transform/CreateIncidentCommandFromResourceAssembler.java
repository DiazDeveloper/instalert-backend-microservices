package com.instalert_backend.incidents.interfaces.rest.transform;

import com.instalert_backend.incidents.domain.model.commands.CreateIncidentCommand;
import com.instalert_backend.incidents.interfaces.rest.resources.CreateIncidentResource;

public class CreateIncidentCommandFromResourceAssembler {

    public static CreateIncidentCommand toCommandFromResource(CreateIncidentResource resource) {
        return new CreateIncidentCommand(
                resource.userId(),
                resource.type(),
                resource.severity(),
                resource.address(),
                resource.description(),
                resource.status(),
                resource.latitude(),
                resource.longitude(),
                resource.timeReported()
        );
    }
}