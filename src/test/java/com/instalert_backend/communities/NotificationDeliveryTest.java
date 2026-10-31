package com.instalert_backend.communities;
import com.instalert_backend.communities.notifications.CommunityNotifications;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest
class NotificationDeliveryTest {
 @Autowired CommunityNotifications service;@Autowired JdbcTemplate db;@Autowired ObjectMapper json;
 String owner=UUID.randomUUID().toString(),other=UUID.randomUUID().toString(),stranger=UUID.randomUUID().toString();
 @BeforeEach void clear(){for(String table:List.of("community_alert_inbox","community_alert_recipients","community_processed_alert_events","notification_contacts","community_notification_preferences"))db.update("DELETE FROM "+table);}
 String link(){String id=service.invite(owner,other).get("id").toString();service.accept(other,id);return id;}
 com.fasterxml.jackson.databind.JsonNode alert(String id,String status){return json.valueToTree(Map.of("id",id,"accountId",owner,"status",status,"createdAt",java.time.Instant.now().plusSeconds(1).toString(),"latitude",-12.0,"longitude",-77.0));}
 @Test void consentDedupCancellationAndPrivacy(){
  link();String id=UUID.randomUUID().toString(),event=UUID.randomUUID().toString();
  service.receive(event,"EmergencyAlertCreated",alert(id,"ACTIVE"));service.receive(event,"EmergencyAlertCreated",alert(id,"ACTIVE"));
  assertThat(service.inbox(other,0)).hasSize(1);assertThat(service.inbox(stranger,0)).isEmpty();assertThat(service.inbox(owner,0)).isEmpty();
  service.preference(other,false);service.receive(UUID.randomUUID().toString(),"EmergencyAlertCancelled",alert(id,"CANCELLED"));assertThat(service.inbox(other,0)).hasSize(2);
 }
 @Test void pendingAndDisabledContactsDoNotReceive(){
  String id=service.invite(owner,other).get("id").toString();
  assertThatThrownBy(()->service.accept(owner,id)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
  service.receive(UUID.randomUUID().toString(),"EmergencyAlertCreated",alert(UUID.randomUUID().toString(),"ACTIVE"));assertThat(service.inbox(other,0)).isEmpty();
  service.accept(other,id);service.preference(other,false);
  service.receive(UUID.randomUUID().toString(),"EmergencyAlertCreated",alert(UUID.randomUUID().toString(),"ACTIVE"));assertThat(service.inbox(other,0)).isEmpty();
 }
 @Test void revokedContactAndHistoricalEventsDoNotLeak(){
  String link=link();var old=json.valueToTree(Map.of("id",UUID.randomUUID().toString(),"accountId",owner,"status","ACTIVE","createdAt","2020-01-01T00:00:00Z"));
  service.receive(UUID.randomUUID().toString(),"EmergencyAlertCreated",old);assertThat(service.inbox(other,0)).isEmpty();
  service.revoke(other,link);service.receive(UUID.randomUUID().toString(),"EmergencyAlertCreated",alert(UUID.randomUUID().toString(),"ACTIVE"));assertThat(service.inbox(other,0)).isEmpty();
 }
}
