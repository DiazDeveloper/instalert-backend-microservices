package com.instalert_backend.incidents.application.commandservices;

import com.instalert_backend.incidents.domain.model.aggregates.Incident;
import com.instalert_backend.incidents.domain.model.commands.CreateIncidentCommand;
import com.instalert_backend.incidents.domain.model.commands.DeleteIncidentCommand;
import com.instalert_backend.incidents.domain.model.commands.UpdateIncidentStatusCommand;
import com.instalert_backend.shared.application.result.ApplicationError;
import com.instalert_backend.shared.application.result.Result;

public interface IncidentCommandService {

    Result<Incident, ApplicationError> handle(CreateIncidentCommand command);

    Result<Void, ApplicationError> handle(DeleteIncidentCommand command);

    Result<Incident, ApplicationError> handle(UpdateIncidentStatusCommand command);
}