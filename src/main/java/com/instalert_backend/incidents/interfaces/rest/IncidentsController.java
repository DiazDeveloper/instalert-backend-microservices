package com.instalert_backend.incidents.interfaces.rest;

import com.instalert_backend.incidents.application.commandservices.IncidentCommandService;
import com.instalert_backend.incidents.application.queryservices.IncidentQueryService;
import com.instalert_backend.incidents.domain.model.queries.GetAllIncidentsQuery;
import com.instalert_backend.incidents.domain.model.queries.GetIncidentByIdQuery;
import com.instalert_backend.incidents.interfaces.rest.resources.CreateIncidentResource;
import com.instalert_backend.incidents.interfaces.rest.resources.IncidentResource;
import com.instalert_backend.incidents.interfaces.rest.transform.CreateIncidentCommandFromResourceAssembler;
import com.instalert_backend.incidents.interfaces.rest.transform.IncidentResourceFromEntityAssembler;
import com.instalert_backend.shared.application.result.ApplicationError;
import com.instalert_backend.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.instalert_backend.shared.interfaces.rest.transform.ResponseEntityAssembler;
import com.instalert_backend.incidents.domain.model.commands.DeleteIncidentCommand;
import com.instalert_backend.incidents.domain.model.commands.UpdateIncidentStatusCommand;
import com.instalert_backend.incidents.interfaces.rest.resources.UpdateIncidentStatusResource;
import com.instalert_backend.incidents.interfaces.rest.transform.UpdateIncidentStatusCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/incidents", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Incidents", description = "Incident management endpoints")
public class IncidentsController {

    private final IncidentCommandService incidentCommandService;
    private final IncidentQueryService incidentQueryService;

    public IncidentsController(IncidentCommandService incidentCommandService, IncidentQueryService incidentQueryService) {
        this.incidentCommandService = incidentCommandService;
        this.incidentQueryService = incidentQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new incident",
            description = "Creates a new incident report with location and emergency information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Incident created successfully",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<?> createIncident(@Valid @RequestBody CreateIncidentResource resource) {
        com.instalert_backend.transition.ReferenceAuthorization.requireOwner(resource.userId());
        var createIncidentCommand = CreateIncidentCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = incidentCommandService.handle(createIncidentCommand);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                IncidentResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Get all incidents",
            description = "Retrieves a list of all incident reports in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Incidents found",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<IncidentResource>> getAllIncidents() {
        var incidents = incidentQueryService.handle(new GetAllIncidentsQuery());

        if (incidents.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        var incidentResources = incidents.stream()
                .map(IncidentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(incidentResources);
    }

    @Operation(
            summary = "Get incident by ID",
            description = "Retrieves a specific incident report by unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident found",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    @GetMapping("/{incidentId}")
    public ResponseEntity<?> getIncidentById(
            @PathVariable
            @Parameter(description = "Incident unique identifier", example = "1", required = true)
            Long incidentId
    ) {
        var getIncidentByIdQuery = new GetIncidentByIdQuery(incidentId);
        var incident = incidentQueryService.handle(getIncidentByIdQuery);

        if (incident.isEmpty()) {
            var error = ApplicationError.notFound("Incident", incidentId.toString());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }

        var incidentResource = IncidentResourceFromEntityAssembler.toResourceFromEntity(incident.get());
        return ResponseEntity.ok(incidentResource);
    }
    @Operation(
            summary = "Delete an incident",
            description = "Deletes an incident report by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Incident deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    @DeleteMapping("/{incidentId}")
    public ResponseEntity<?> deleteIncident(
            @PathVariable
            @Parameter(description = "Incident unique identifier", example = "1", required = true)
            Long incidentId
    ) {
        var existing = incidentQueryService.handle(new GetIncidentByIdQuery(incidentId));
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        com.instalert_backend.transition.ReferenceAuthorization.requireOwnerOrAdmin(existing.get().getUserId());
        var command = new DeleteIncidentCommand(incidentId);
        var result = incidentCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                v -> null,
                HttpStatus.NO_CONTENT
        );
    }
    @Operation(
            summary = "Update incident status",
            description = "Updates the status of an existing incident report."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident status updated successfully",
                    content = @Content(schema = @Schema(implementation = IncidentResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    @PutMapping("/{incidentId}/status")
    public ResponseEntity<?> updateIncidentStatus(
            @PathVariable
            @Parameter(description = "Incident unique identifier", example = "1", required = true)
            Long incidentId,
            @Valid @RequestBody UpdateIncidentStatusResource resource
    ) {
        var existing = incidentQueryService.handle(new GetIncidentByIdQuery(incidentId));
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        com.instalert_backend.transition.ReferenceAuthorization.requireOwnerOrAdmin(existing.get().getUserId());
        var command = UpdateIncidentStatusCommandFromResourceAssembler.toCommandFromResource(incidentId, resource);
        var result = incidentCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                IncidentResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }
}