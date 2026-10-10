package com.instalert_backend.incidents.application.internal.commandservices;

import com.instalert_backend.incidents.application.commandservices.IncidentCommandService;
import com.instalert_backend.incidents.domain.model.aggregates.Incident;
import com.instalert_backend.incidents.domain.model.commands.CreateIncidentCommand;
import com.instalert_backend.incidents.domain.model.commands.DeleteIncidentCommand;
import com.instalert_backend.incidents.domain.model.commands.UpdateIncidentStatusCommand;
import com.instalert_backend.incidents.domain.repositories.IncidentRepository;
import com.instalert_backend.shared.application.result.ApplicationError;
import com.instalert_backend.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class IncidentCommandServiceImpl implements IncidentCommandService {

    private final IncidentRepository incidentRepository;

    public IncidentCommandServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public Result<Incident, ApplicationError> handle(CreateIncidentCommand command) {
        try {
            var incident = new Incident(command);
            var savedIncident = incidentRepository.save(incident);
            return Result.success(savedIncident);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Incident", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Incident creation", e.getMessage()));
        }
    }

    @Override
    public Result<Void, ApplicationError> handle(DeleteIncidentCommand command) {
        try {
            var existing = incidentRepository.findById(command.id());
            if (existing.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Incident", command.id().toString()));
            }
            incidentRepository.deleteById(command.id());
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Incident deletion", e.getMessage()));
        }
    }

    @Override
    public Result<Incident, ApplicationError> handle(UpdateIncidentStatusCommand command) {
        try {
            var existing = incidentRepository.findById(command.id());
            if (existing.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Incident", command.id().toString()));
            }
            var incident = existing.get();
            incident.updateStatus(command.status());
            var updated = incidentRepository.save(incident);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Incident", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Incident status update", e.getMessage()));
        }
    }
}