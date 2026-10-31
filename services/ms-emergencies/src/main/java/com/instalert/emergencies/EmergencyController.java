package com.instalert.emergencies;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.dao.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.net.URI;
import java.util.List;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/emergencies")
public class EmergencyController {
 private final EmergencyService service;
 EmergencyController(EmergencyService service) { this.service=service; }
 public record CreateAlert(@NotBlank @Size(max=100) String type,
  @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
  @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
  @PositiveOrZero Double accuracyM,@NotNull Instant locationRecordedAt) {}
 public record CancelAlert(@NotBlank @Pattern(regexp="CANCELLED") String status) {}
 public record AlertResponse(String id,String accountId,String type,double latitude,double longitude,Double accuracyM,
  Instant locationRecordedAt,Instant createdAt,Instant cancelledAt,String status,long version) {
  static AlertResponse from(EmergencyAlert a) { return new AlertResponse(a.id,a.ownerAccountId,a.alertType,a.latitude,a.longitude,a.accuracyM,a.locationRecordedAt,a.createdAt,a.cancelledAt,a.status,a.version); }
 }
 public record History(List<AlertResponse> content,int page,int size,long totalElements,int totalPages) {}
 private String owner(Authentication auth) { return ((AccountSecurityConfiguration.AccountPrincipal)auth.getPrincipal()).accountId(); }
 private String id(String value) { try {return UUID.fromString(value).toString();} catch(IllegalArgumentException e) {throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid alert UUID");} }
 @PostMapping public ResponseEntity<AlertResponse> create(Authentication auth,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody CreateAlert c) {
  if(!key.matches("[A-Za-z0-9_-]{8,128}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid idempotency key");
  EmergencyService.Creation result;
  try { result=service.create(owner(auth),key,c); }
  catch(DataIntegrityViolationException e) { result=service.replayAfterConflict(owner(auth),key,c); }
  return ResponseEntity.status(result.replayed()?200:201).location(URI.create("/api/v1/emergencies/"+result.alert().id))
   .header("Idempotency-Replayed",Boolean.toString(result.replayed())).body(AlertResponse.from(result.alert()));
 }
 @GetMapping("/{id}") public AlertResponse get(Authentication auth,@PathVariable String id) {return AlertResponse.from(service.get(owner(auth),id(id)));}
 @PatchMapping("/{id}") public AlertResponse cancel(Authentication auth,@PathVariable String id,@Valid @RequestBody CancelAlert c) {return AlertResponse.from(service.cancel(owner(auth),id(id)));}
 @GetMapping public History history(Authentication auth,@RequestParam(required=false) String status,@RequestParam(required=false) String type,
  @RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
  if(page<0 || size<1 || size>100 || (status!=null && !List.of("ACTIVE","CANCELLED").contains(status)) || (from!=null && to!=null && from.isAfter(to)))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid history filters");
  var p=service.history(owner(auth),status,type,from,to,page,size);
  return new History(p.getContent().stream().map(AlertResponse::from).toList(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());
 }
}
