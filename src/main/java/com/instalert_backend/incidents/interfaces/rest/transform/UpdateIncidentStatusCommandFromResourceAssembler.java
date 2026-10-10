package com.instalert_backend.incidents.interfaces.rest.transform;

import com.instalert_backend.incidents.domain.model.commands.UpdateIncidentStatusCommand;
import com.instalert_backend.incidents.interfaces.rest.resources.UpdateIncidentStatusResource;

public class UpdateIncidentStatusCommandFromResourceAssembler {
    public static UpdateIncidentStatusCommand toCommandFromResource(Long id, UpdateIncidentStatusResource resource) {
        return new UpdateIncidentStatusCommand(id, resource.status());
    }
}