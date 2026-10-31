package com.instalert.emergencies;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="emergency_alerts")
public class EmergencyAlert {
 @Id public String id;
 @Column(nullable=false) public String ownerAccountId;
 @Column(nullable=false) public String alertType;
 public double latitude,longitude;
 @Column(name="accuracy_m") public Double accuracyM;
 @Column(nullable=false) public Instant locationRecordedAt,createdAt;
 public Instant cancelledAt;
 @Column(nullable=false) public String status;
 @Version public long version;
 protected EmergencyAlert() {}
 public EmergencyAlert(String owner,EmergencyController.CreateAlert c) {
  id=UUID.randomUUID().toString();ownerAccountId=owner;alertType=c.type();
  latitude=c.latitude();longitude=c.longitude();accuracyM=c.accuracyM();
  locationRecordedAt=c.locationRecordedAt();createdAt=Instant.now();status="ACTIVE";
 }
 public void cancel() { if("ACTIVE".equals(status)) { status="CANCELLED";cancelledAt=Instant.now(); } }
}
