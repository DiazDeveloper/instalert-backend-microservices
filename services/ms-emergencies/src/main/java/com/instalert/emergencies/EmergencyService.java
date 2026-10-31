package com.instalert.emergencies;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
@Service
public class EmergencyService {
 private final EmergencyRepository alerts;
 private final CreationRequestRepository requests;
 private final OutboxRepository outbox;
 private final ObjectMapper json;
 EmergencyService(EmergencyRepository a,CreationRequestRepository r,OutboxRepository o,ObjectMapper j) { alerts=a;requests=r;outbox=o;json=j; }
 public record Creation(EmergencyAlert alert,boolean replayed) {}
 private String hash(EmergencyController.CreateAlert c) {
  try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.writeValueAsBytes(c))); }
  catch(Exception e) { throw new IllegalStateException(e); }
 }
 private Creation replay(CreationRequest r,String fingerprint) {
  if(!r.requestHash.equals(fingerprint)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Idempotency key already used for another request");
  return new Creation(alerts.findById(r.alertId).orElseThrow(),true);
 }
 @Transactional(readOnly=true)
 public Creation replayAfterConflict(String owner,String key,EmergencyController.CreateAlert c) {
  return replay(requests.findByOwnerAccountIdAndRequestKey(owner,key).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"Concurrent update; retry request")),hash(c));
 }
 @Transactional
 public Creation create(String owner,String key,EmergencyController.CreateAlert c) {
  var fingerprint=hash(c);var previous=requests.findByOwnerAccountIdAndRequestKey(owner,key);
  if(previous.isPresent()) return replay(previous.get(),fingerprint);
  if(c.locationRecordedAt().isAfter(Instant.now().plusSeconds(60)) || c.locationRecordedAt().isBefore(Instant.now().minusSeconds(600)))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Location must be recent");
  var a=alerts.saveAndFlush(new EmergencyAlert(owner,c));
  requests.saveAndFlush(new CreationRequest(owner,key,fingerprint,a.id));
  event(a,"EmergencyAlertCreated");return new Creation(a,false);
 }
 private void event(EmergencyAlert a,String type) {
  try { outbox.save(new OutboxEvent(a,type,json.writeValueAsString(EmergencyController.AlertResponse.from(a)))); }
  catch(com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException(e); }
 }
 @Transactional(readOnly=true)
 public EmergencyAlert get(String owner,String id) {
  var a=alerts.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Alert not found"));
  if(!a.ownerAccountId.equals(owner)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Alert belongs to another account");
  return a;
 }
 @Transactional
 public EmergencyAlert cancel(String owner,String id) {
  var a=get(owner,id);
  if("ACTIVE".equals(a.status)) { a.cancel();alerts.flush();event(a,"EmergencyAlertCancelled"); }
  return a;
 }
 @Transactional(readOnly=true)
 public Page<EmergencyAlert> history(String owner,String status,String type,Instant from,Instant to,int page,int size) {
  Specification<EmergencyAlert> spec=(r,q,b)->b.equal(r.get("ownerAccountId"),owner);
  if(status!=null) spec=spec.and((r,q,b)->b.equal(r.get("status"),status));
  if(type!=null) spec=spec.and((r,q,b)->b.equal(r.get("alertType"),type));
  if(from!=null) spec=spec.and((r,q,b)->b.greaterThanOrEqualTo(r.get("createdAt"),from));
  if(to!=null) spec=spec.and((r,q,b)->b.lessThanOrEqualTo(r.get("createdAt"),to));
  return alerts.findAll(spec,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt").and(Sort.by("id"))));
 }
}
