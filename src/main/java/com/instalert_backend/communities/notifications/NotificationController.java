package com.instalert_backend.communities.notifications;
import com.fasterxml.jackson.databind.JsonNode;
import com.instalert_backend.transition.AccountSecurityConfiguration.AccountPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import java.util.*;
@RestController
@RequestMapping("/api/v1/communities")
public class NotificationController {
 private final CommunityNotifications service;
 public NotificationController(CommunityNotifications service){this.service=service;}
 public record Invite(@NotNull UUID accountId){}
 public record Preference(@NotNull Boolean enabled){}
 public record Event(@NotNull UUID eventId,@NotBlank String eventType,@NotNull JsonNode alert){}
 @GetMapping("/contacts") public Object contacts(@AuthenticationPrincipal AccountPrincipal p){return service.contacts(p.accountId());}
 @PostMapping("/contacts") @ResponseStatus(HttpStatus.CREATED) public Object invite(@AuthenticationPrincipal AccountPrincipal p,@Valid @RequestBody Invite body){return service.invite(p.accountId(),body.accountId().toString());}
 @PutMapping("/contacts/{id}/accept") @ResponseStatus(HttpStatus.NO_CONTENT) public void accept(@AuthenticationPrincipal AccountPrincipal p,@PathVariable UUID id){service.accept(p.accountId(),id.toString());}
 @DeleteMapping("/contacts/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void revoke(@AuthenticationPrincipal AccountPrincipal p,@PathVariable UUID id){service.revoke(p.accountId(),id.toString());}
 @GetMapping("/notifications") public Object inbox(@AuthenticationPrincipal AccountPrincipal p,@RequestParam(defaultValue="0") @Min(0) @Max(10000) int page){return service.inbox(p.accountId(),page);}
 @PutMapping("/notifications/{id}/read") @ResponseStatus(HttpStatus.NO_CONTENT) public void read(@AuthenticationPrincipal AccountPrincipal p,@PathVariable UUID id){service.read(p.accountId(),id.toString());}
 @GetMapping("/notification-preferences") public Object preference(@AuthenticationPrincipal AccountPrincipal p){return Map.of("enabled",service.enabled(p.accountId()));}
 @PutMapping("/notification-preferences") @ResponseStatus(HttpStatus.NO_CONTENT) public void preference(@AuthenticationPrincipal AccountPrincipal p,@Valid @RequestBody Preference body){service.preference(p.accountId(),body.enabled());}
 @io.swagger.v3.oas.annotations.Hidden
 @PostMapping("/internal/emergency-events") @ResponseStatus(HttpStatus.NO_CONTENT) public void event(@Valid @RequestBody Event body){service.receive(body.eventId().toString(),body.eventType(),body.alert());}
}
