package com.instalert_backend.emergencies.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EmergencyAlertResponse")
public record EmergencyAlertResource(
        Long id,
        Long userId,
        String type,
        String location,
        String time,
        String status,
        String statusClass
) {
}