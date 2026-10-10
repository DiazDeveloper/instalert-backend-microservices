package com.instalert_backend.emergencies.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "UpdateEmergencyStatusRequest")
public record UpdateEmergencyStatusResource(
        @NotBlank
        String status,
        @NotBlank
        String statusClass
) {
}