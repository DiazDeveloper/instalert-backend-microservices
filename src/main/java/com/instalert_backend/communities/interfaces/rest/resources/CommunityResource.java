package com.instalert_backend.communities.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommunityResource(
        @Schema(description = "Identificador único", example = "1")
        Long id,

        @Schema(description = "Nombre de la comunidad", example = "Vecinos Cuadra 5")
        String name,

        @Schema(description = "Descripción", example = "Grupo exclusivo de seguridad")
        String description,

        @Schema(description = "Si es privada", example = "true")
        boolean isPrivate,

        @Schema(description = "ID del creador de la comunidad", example = "1")
        Long ownerId
) {}