package com.instalert_backend.emergencies.domain.model.aggregates;

import com.instalert_backend.emergencies.domain.model.commands.CreateEmergencyAlertCommand;
import com.instalert_backend.emergencies.domain.model.events.EmergencyAlertCreatedEvent;
import com.instalert_backend.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.Objects;

public class EmergencyAlert extends AbstractDomainAggregateRoot<EmergencyAlert> {

    @Getter
    private Long id;

    @Getter
    private Long userId;

    @Getter
    private String type;

    @Getter
    private String location;

    @Getter
    private String time;

    @Getter
    private String status;

    @Getter
    private String statusClass;

    public EmergencyAlert() {
    }

    public EmergencyAlert(Long id, Long userId, String type, String location, String time, String status, String statusClass) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.location = location;
        this.time = time;
        this.status = status;
        this.statusClass = statusClass;
    }

    public EmergencyAlert(CreateEmergencyAlertCommand command) {
        this.userId = Objects.requireNonNull(command.userId(), "userId no puede ser nulo");
        this.type = Objects.requireNonNull(command.type(), "type no puede ser nulo");
        this.location = Objects.requireNonNull(command.location(), "location no puede ser nulo");
        this.time = Objects.requireNonNull(command.time(), "time no puede ser nulo");
        this.status = Objects.requireNonNull(command.status(), "status no puede ser nulo");
        this.statusClass = Objects.requireNonNull(command.statusClass(), "statusClass no puede ser nulo");
    }

    public void updateStatus(String newStatus, String newStatusClass) {
        this.status = Objects.requireNonNull(newStatus, "El nuevo estado no puede ser nulo");
        this.statusClass = Objects.requireNonNull(newStatusClass, "La nueva clase de estado no puede ser nula");
    }

    public void onCreated() {
        registerDomainEvent(EmergencyAlertCreatedEvent.from(this));
    }
}