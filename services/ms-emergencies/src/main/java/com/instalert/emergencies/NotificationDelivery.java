package com.instalert.emergencies;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.util.Map;

/** At-least-once HTTP delivery; Communities deduplicates event IDs atomically. */
@Service
public class NotificationDelivery {
 private final OutboxRepository outbox;private final ObjectMapper json;private final RestClient client;private final String key;
 NotificationDelivery(OutboxRepository outbox,ObjectMapper json,@Value("${notifications.communities-url:http://localhost:8082}") String url,@Value("${notifications.internal-key:}") String key){
  this.outbox=outbox;this.json=json;this.key=key;
  var factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(1500);factory.setReadTimeout(3000);
  client=RestClient.builder().baseUrl(url).requestFactory(factory).build();
 }
 @Transactional public void deliver(){
  if(key.length()<32)return; // Fail closed until the service credential is configured.
  // One locked event per transaction: preserve creation/cancellation order across workers.
  var pending=outbox.pending(PageRequest.of(0,1));if(pending.isEmpty())return;
  var event=pending.getFirst();event.deliveryAttempts++;
  try{
   client.post().uri("/api/v1/communities/internal/emergency-events").header("X-Notification-Key",key)
    .body(Map.of("eventId",event.id,"eventType",event.eventType,"alert",json.readTree(event.payload))).retrieve().toBodilessEntity();
   event.publicationStatus="PUBLISHED";event.publishedAt=Instant.now();
  }catch(Exception failure){
   // Keep durable PENDING state; retry the same ID after a timeout or receiver restart.
   org.slf4j.LoggerFactory.getLogger(getClass()).warn("Notification delivery pending; event={}, attempt={}",event.id,event.deliveryAttempts);
  }
 }
}
