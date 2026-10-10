package com.instalert_backend.communities.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommunityResource(
        @NotBlank(message = "El nombre de la comunidad es requerido")
        @Schema(description = "Nombre de la comunidad", example = "Vecinos Cuadra 5")
        String name,

        @Schema(description = "Descripción o reglas", example = "Grupo exclusivo de seguridad")
        String description,

        @Schema(description = "Si la comunidad es privada por invitación", example = "true")
        boolean isPrivate,

        @NotNull(message = "El ID del creador es requerido")
        @Schema(description = "ID del usuario que crea la comunidad", example = "1")
        Long ownerId
) {}