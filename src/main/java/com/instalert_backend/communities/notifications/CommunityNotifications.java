package com.instalert_backend.communities.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instalert_backend.transition.AccountSecurityConfiguration.AccountPrincipal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

/** Communities owns consent, recipient selection and durable in-app delivery. */
@Service
public class CommunityNotifications {
 private final JdbcTemplate db; private final ObjectMapper json;
 public CommunityNotifications(JdbcTemplate db,ObjectMapper json){this.db=db;this.json=json;}
 private void require(boolean ok,HttpStatus status){if(!ok)throw new ResponseStatusException(status);}
 public List<Map<String,Object>> contacts(String me){return db.queryForList("SELECT id, requester, recipient, status FROM notification_contacts WHERE requester=? OR recipient=?",me,me);}
 @Transactional public Map<String,Object> invite(String me,String target){
  UUID.fromString(target);require(!me.equals(target),HttpStatus.BAD_REQUEST);
  String pair=me.compareTo(target)<0?me+":"+target:target+":"+me;
  var previous=db.queryForList("SELECT id,status FROM notification_contacts WHERE pair_key=?",pair);
  if(!previous.isEmpty())return previous.getFirst();
  String id=UUID.randomUUID().toString();db.update("INSERT INTO notification_contacts(id,pair_key,requester,recipient,status) VALUES(?,?,?,?,'PENDING')",id,pair,me,target);
  return Map.of("id",id,"status","PENDING");
 }
 @Transactional public void accept(String me,String id){
  var rows=db.queryForList("SELECT recipient,status FROM notification_contacts WHERE id=? FOR UPDATE",id);
  require(!rows.isEmpty(),HttpStatus.NOT_FOUND);require(me.equals(rows.getFirst().get("recipient")),HttpStatus.FORBIDDEN);
  require(!"REVOKED".equals(rows.getFirst().get("status")),HttpStatus.CONFLICT);
  db.update("UPDATE notification_contacts SET status='ACCEPTED',accepted_at=COALESCE(accepted_at,CURRENT_TIMESTAMP) WHERE id=?",id);
 }
 @Transactional public void revoke(String me,String id){require(db.update("UPDATE notification_contacts SET status='REVOKED' WHERE id=? AND (requester=? OR recipient=?)",id,me,me)>0,HttpStatus.NOT_FOUND);}
 public List<Map<String,Object>> inbox(String me,int page){return db.queryForList("SELECT id,event_id,alert_id,event_type,payload,received_at,read_at FROM community_alert_inbox WHERE recipient=? ORDER BY received_at DESC,id LIMIT 50 OFFSET ?",me,page*50);}
 public void read(String me,String id){require(db.update("UPDATE community_alert_inbox SET read_at=COALESCE(read_at,CURRENT_TIMESTAMP) WHERE id=? AND recipient=?",id,me)>0,HttpStatus.NOT_FOUND);}
 public boolean enabled(String me){var v=db.queryForList("SELECT enabled FROM community_notification_preferences WHERE account_id=?",me);return v.isEmpty() || ((Number)v.getFirst().get("enabled")).intValue()==1;}
 @Transactional public void preference(String me,boolean enabled){db.update("DELETE FROM community_notification_preferences WHERE account_id=?",me);db.update("INSERT INTO community_notification_preferences(account_id,enabled) VALUES(?,?)",me,enabled?1:0);}
 @Transactional public void receive(String eventId,String type,JsonNode alert){
  UUID.fromString(eventId);String alertId=alert.path("id").asText();String owner=alert.path("accountId").asText();UUID.fromString(alertId);UUID.fromString(owner);
  require(Set.of("EmergencyAlertCreated","EmergencyAlertCancelled").contains(type),HttpStatus.BAD_REQUEST);
  require((type.equals("EmergencyAlertCreated")?"ACTIVE":"CANCELLED").equals(alert.path("status").asText()),HttpStatus.BAD_REQUEST);
  if(!db.queryForList("SELECT event_id FROM community_processed_alert_events WHERE event_id=?",eventId).isEmpty())return;
  db.update("INSERT INTO community_processed_alert_events(event_id,alert_id) VALUES(?,?)",eventId,alertId);
  List<String> recipients;
  if(type.equals("EmergencyAlertCreated")){
   recipients=db.query("SELECT CASE WHEN requester=? THEN recipient ELSE requester END AS target FROM notification_contacts WHERE status='ACCEPTED' AND accepted_at<=? AND (requester=? OR recipient=?)",(rs,n)->rs.getString("target"),owner,java.sql.Timestamp.from(java.time.Instant.parse(alert.path("createdAt").asText())),owner,owner);
   recipients=recipients.stream().filter(this::enabled).distinct().toList();
   for(String target:recipients)db.update("INSERT INTO community_alert_recipients(alert_id,recipient) VALUES(?,?)",alertId,target);
  }else{
   // Cancellation reaches the same recipients who received creation, even after opt-out.
   recipients=db.query("SELECT recipient FROM community_alert_recipients WHERE alert_id=?",(rs,n)->rs.getString(1),alertId);
  }
  for(String target:recipients)db.update("INSERT INTO community_alert_inbox(id,event_id,alert_id,recipient,event_type,payload) VALUES(?,?,?,?,?,?)",UUID.randomUUID().toString(),eventId,alertId,target,type,alert.toString());
 }
}
