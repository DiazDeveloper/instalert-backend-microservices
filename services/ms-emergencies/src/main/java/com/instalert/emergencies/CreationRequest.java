package com.instalert.emergencies;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="alert_creation_requests",uniqueConstraints=@UniqueConstraint(columnNames={"owner_account_id","request_key"}))
public class CreationRequest {
 @Id public String id;
 public String ownerAccountId,requestKey,requestHash,alertId;
 public Instant createdAt;
 protected CreationRequest() {}
 CreationRequest(String owner,String key,String hash,String alert) {
  id=UUID.randomUUID().toString();ownerAccountId=owner;requestKey=key;requestHash=hash;alertId=alert;createdAt=Instant.now();
 }
}
