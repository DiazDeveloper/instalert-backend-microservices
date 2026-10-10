package com.instalert_backend.communities.interfaces.rest;

import com.instalert_backend.communities.application.commandservices.CommunityCommandService;
import com.instalert_backend.communities.application.queryservices.CommunityQueryService;
import com.instalert_backend.communities.domain.model.commands.DeleteCommunityCommand;
import com.instalert_backend.communities.domain.model.queries.GetAllCommunitiesQuery;
import com.instalert_backend.communities.domain.model.queries.GetCommunityByIdQuery;
import com.instalert_backend.communities.interfaces.rest.resources.CommunityResource;
import com.instalert_backend.communities.interfaces.rest.resources.CreateCommunityResource;
import com.instalert_backend.communities.interfaces.rest.transform.CommunityResourceFromEntityAssembler;
import com.instalert_backend.communities.interfaces.rest.transform.CreateCommunityCommandFromResourceAssembler;
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
@RequestMapping(value = "/api/v1/communities", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Communities", description = "Endpoints para la gestión de juntas vecinales")
public class CommunitiesController {

    private final CommunityCommandService communityCommandService;
    private final CommunityQueryService communityQueryService;

    public CommunitiesController(CommunityCommandService communityCommandService, CommunityQueryService communityQueryService) {
        this.communityCommandService = communityCommandService;
        this.communityQueryService = communityQueryService;
    }

    @PostMapping
    public ResponseEntity<?> createCommunity(@Valid @RequestBody CreateCommunityResource resource) {
        com.instalert_backend.transition.ReferenceAuthorization.requireOwner(resource.ownerId());
        var createCommunityCommand = CreateCommunityCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = communityCommandService.handle(createCommunityCommand);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CommunityResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<CommunityResource>> getAllCommunities() {
        var communities = communityQueryService.handle(new GetAllCommunitiesQuery());
        if (communities.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        var resources = communities.stream()
                .map(CommunityResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{communityId}")
    public ResponseEntity<?> getCommunityById(@PathVariable Long communityId) {
        var query = new GetCommunityByIdQuery(communityId);
        var community = communityQueryService.handle(query);

        if (community.isEmpty()) {
            var error = ApplicationError.notFound("Community", communityId.toString());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }

        var resource = CommunityResourceFromEntityAssembler.toResourceFromEntity(community.get());
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{communityId}")
    public ResponseEntity<?> deleteCommunity(@PathVariable Long communityId) {
        var existing = communityQueryService.handle(new GetCommunityByIdQuery(communityId));
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        com.instalert_backend.transition.ReferenceAuthorization.requireOwnerOrAdmin(existing.get().getOwnerId());
        var command = new DeleteCommunityCommand(communityId);
        try {
            communityCommandService.handle(command);
            // Devuelve 204 No Content, que es el estándar HTTP cuando se borra algo con éxito
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Devuelve 404 Not Found si intentas borrar un ID que no existe
            return ResponseEntity.notFound().build();
        }
    }
}