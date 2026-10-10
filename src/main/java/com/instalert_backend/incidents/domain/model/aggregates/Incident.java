package com.instalert_backend.incidents.domain.model.aggregates;

import com.instalert_backend.incidents.domain.model.commands.CreateIncidentCommand;
import com.instalert_backend.incidents.domain.model.events.IncidentCreatedEvent;
import com.instalert_backend.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Incident extends AbstractDomainAggregateRoot<Incident> {
    @Getter
    @Setter
    private Long id;

    @Getter
    private Long userId;

    @Getter
    private String type;

    @Getter
    private String severity;

    @Getter
    private String address;

    @Getter
    private String description;

    @Getter
    private String status;

    @Getter
    private Double latitude;

    @Getter
    private Double longitude;

    @Getter
    private String timeReported;

    public Incident(Long id, Long userId, String type, String severity, String address,
                    String description, String status, Double latitude, Double longitude,
                    String timeReported) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.address = Objects.requireNonNull(address, "address must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.latitude = Objects.requireNonNull(latitude, "latitude must not be null");
        this.longitude = Objects.requireNonNull(longitude, "longitude must not be null");
        this.timeReported = Objects.requireNonNull(timeReported, "timeReported must not be null");
    }

    public Incident(Long userId, String type, String severity, String address,
                    String description, String status, Double latitude, Double longitude,
                    String timeReported) {
        this(null, userId, type, severity, address, description, status, latitude, longitude, timeReported);
    }

    public Incident(CreateIncidentCommand command) {
        this(
                command.userId(),
                command.type(),
                command.severity(),
                command.address(),
                command.description(),
                command.status(),
                command.latitude(),
                command.longitude(),
                command.timeReported()
        );
    }
    public void updateStatus(String status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public void onCreated() {
        registerDomainEvent(IncidentCreatedEvent.from(this));
    }
}