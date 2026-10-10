package com.instalert_backend.emergencies.application.internal.eventhandlers;

import com.instalert_backend.emergencies.domain.model.events.EmergencyAlertCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service("emergenciesEmergencyAlertCreatedEventHandler")
public class EmergencyAlertCreatedEventHandler {

    @EventListener
    public void on(EmergencyAlertCreatedEvent event) {
        System.out.println("Alerta Creada: " + event.alertId() + " en " + event.location());
    }
}