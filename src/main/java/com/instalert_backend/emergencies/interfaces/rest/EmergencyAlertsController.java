package com.instalert_backend.emergencies.interfaces.rest;

import com.instalert_backend.emergencies.application.commandservices.EmergencyAlertCommandService;
import com.instalert_backend.emergencies.application.queryservices.EmergencyAlertQueryService;
import com.instalert_backend.emergencies.domain.model.queries.GetAllEmergencyAlertsQuery;
import com.instalert_backend.emergencies.domain.model.queries.GetEmergencyAlertByIdQuery;
import com.instalert_backend.emergencies.interfaces.rest.resources.CreateEmergencyAlertResource;
import com.instalert_backend.emergencies.interfaces.rest.resources.EmergencyAlertResource;
import com.instalert_backend.emergencies.interfaces.rest.resources.UpdateEmergencyStatusResource;
import com.instalert_backend.emergencies.interfaces.rest.transform.CreateEmergencyAlertCommandFromResourceAssembler;
import com.instalert_backend.emergencies.interfaces.rest.transform.EmergencyAlertResourceFromEntityAssembler;
import com.instalert_backend.emergencies.interfaces.rest.transform.UpdateEmergencyStatusCommandFromResourceAssembler;
import com.instalert_backend.shared.application.result.ApplicationError;
import com.instalert_backend.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.instalert_backend.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/emergencies", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Emergencies")
public class EmergencyAlertsController {

    private final EmergencyAlertCommandService commandService;
    private final EmergencyAlertQueryService queryService;

    public EmergencyAlertsController(EmergencyAlertCommandService commandService, EmergencyAlertQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<?> createEmergencyAlert(@Valid @RequestBody CreateEmergencyAlertResource resource) {
        com.instalert_backend.transition.ReferenceAuthorization.requireOwner(resource.userId());
        var command = CreateEmergencyAlertCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = commandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                EmergencyAlertResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<EmergencyAlertResource>> getAllEmergencyAlerts() {
        var alerts = queryService.handle(new GetAllEmergencyAlertsQuery());
        if (alerts.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        var resources = alerts.stream()
                .map(EmergencyAlertResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmergencyAlertById(@PathVariable Long id) {
        var query = new GetEmergencyAlertByIdQuery(id);
        var alert = queryService.handle(query);
        if (alert.isEmpty()) {
            var error = ApplicationError.notFound("EmergencyAlert", id.toString());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }
        var resource = EmergencyAlertResourceFromEntityAssembler.toResourceFromEntity(alert.get());
        return ResponseEntity.ok(resource);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEmergencyStatus(@PathVariable Long id, @Valid @RequestBody UpdateEmergencyStatusResource resource) {
        var existing = queryService.handle(new GetEmergencyAlertByIdQuery(id));
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        com.instalert_backend.transition.ReferenceAuthorization.requireOwnerOrAdmin(existing.get().getUserId());
        var command = UpdateEmergencyStatusCommandFromResourceAssembler.toCommandFromResource(id, resource);
        var result = commandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                EmergencyAlertResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }
}