package com.instalert_backend.emergencies.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateEmergencyAlertRequest")
public record CreateEmergencyAlertResource(
        @NotNull
        Long userId,
        @NotBlank
        String type,
        @NotBlank
        String location,
        @NotBlank
        String time,
        @NotBlank
        String status,
        @NotBlank
        String statusClass
) {
}