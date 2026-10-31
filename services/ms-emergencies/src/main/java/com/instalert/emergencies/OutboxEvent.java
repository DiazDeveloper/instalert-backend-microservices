package com.instalert.emergencies;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="emergency_outbox_events")
public class OutboxEvent {
 @Id public String id;
 public String alertId,eventType,publicationStatus;
 public long aggregateVersion;
 @Column(columnDefinition="TEXT") public String payload;
 public Instant occurredAt;
 public int deliveryAttempts;
 public Instant publishedAt;
 protected OutboxEvent() {}
 OutboxEvent(EmergencyAlert a,String type,String json) {
  id=UUID.randomUUID().toString();alertId=a.id;eventType=type;aggregateVersion=a.version;
  publicationStatus="PENDING";payload=json;occurredAt=Instant.now();
 }
}
