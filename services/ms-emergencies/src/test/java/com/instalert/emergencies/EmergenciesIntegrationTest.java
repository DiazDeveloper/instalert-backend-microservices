package com.instalert.emergencies;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class EmergenciesIntegrationTest {
 static HttpServer account;
 static {
  try {
   account=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
   account.createContext("/api/v1/account/session",e->{
    String token=e.getRequestHeaders().getFirst("Authorization");
    if("Bearer unavailable".equals(token)) {e.sendResponseHeaders(503,-1);e.close();return;}
    if(!"Bearer owner".equals(token) && !"Bearer other".equals(token)) {e.sendResponseHeaders(401,-1);e.close();return;}
    String id="Bearer owner".equals(token)?"11111111-1111-4111-8111-111111111111":"22222222-2222-4222-8222-222222222222";
    byte[] body=("{\"accountId\":\""+id+"\",\"referenceId\":1,\"roles\":[\"ROLE_USER\"]}").getBytes(StandardCharsets.UTF_8);
    e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(200,body.length);e.getResponseBody().write(body);e.close();
   });account.start();
  } catch(Exception e) {throw new RuntimeException(e);}
 }
 @DynamicPropertySource static void props(DynamicPropertyRegistry r) {r.add("account.service.url",()->"http://127.0.0.1:"+account.getAddress().getPort());}
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Autowired EmergencyRepository alerts;
 @Autowired CreationRequestRepository requests;
 @org.springframework.test.context.bean.override.mockito.MockitoSpyBean OutboxRepository outbox;
 @Autowired EmergencyService service;
 @BeforeEach void clear() {outbox.deleteAll();requests.deleteAll();alerts.deleteAll();}
 @AfterEach void resetSpy() {org.mockito.Mockito.reset(outbox);}
 String body() throws Exception {return json.writeValueAsString(Map.of("type","PANIC","latitude",-12.04,"longitude",-77.04,"locationRecordedAt",Instant.now().toString()));}
 String create(String body,String key) throws Exception {
  return mvc.perform(post("/api/v1/emergencies").header("Authorization","Bearer owner").header("Idempotency-Key",key).contentType("application/json").content(body))
   .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
 }
 @Test void lifecycleOwnershipAndDurableEvents() throws Exception {
  String id=json.readTree(create(body(),"lifecycle-key")).get("id").asText();
  mvc.perform(get("/api/v1/emergencies/"+id).header("Authorization","Bearer other")).andExpect(status().isForbidden());
  mvc.perform(patch("/api/v1/emergencies/"+id).header("Authorization","Bearer other").contentType("application/json").content("{\"status\":\"CANCELLED\"}")).andExpect(status().isForbidden());
  mvc.perform(get("/api/v1/emergencies").header("Authorization","Bearer other")).andExpect(jsonPath("$.totalElements").value(0));
  mvc.perform(get("/api/v1/emergencies").header("Authorization","Bearer owner")).andExpect(jsonPath("$.totalElements").value(1));
  for(int i=0;i<2;i++) mvc.perform(patch("/api/v1/emergencies/"+id).header("Authorization","Bearer owner").contentType("application/json").content("{\"status\":\"CANCELLED\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
  assertThat(outbox.count()).isEqualTo(2);
  assertThat(outbox.findAll()).allMatch(e->e.publicationStatus.equals("PENDING"));
 }
 @Test void retriesAndConflictingPayload() throws Exception {
  String body=body();String id=json.readTree(create(body,"retry-key")).get("id").asText();
  mvc.perform(post("/api/v1/emergencies").header("Authorization","Bearer owner").header("Idempotency-Key","retry-key").contentType("application/json").content(body))
   .andExpect(status().isOk()).andExpect(header().string("Idempotency-Replayed","true")).andExpect(jsonPath("$.id").value(id));
  mvc.perform(post("/api/v1/emergencies").header("Authorization","Bearer owner").header("Idempotency-Key","retry-key").contentType("application/json").content(body.replace("PANIC","OTHER"))).andExpect(status().isConflict());
  assertThat(alerts.count()).isEqualTo(1);assertThat(outbox.count()).isEqualTo(1);
 }
 @Test void rejectsBadInputAndUnauthenticatedCalls() throws Exception {
  mvc.perform(get("/api/v1/emergencies")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/v1/emergencies").header("Authorization","Bearer revoked")).andExpect(status().isUnauthorized());
  for(String invalid:new String[]{body().replace("-12.04","-120.0"),body().replace("PANIC",""),body().replace("\"type\"","\"userId\":99,\"type\"")})
   mvc.perform(post("/api/v1/emergencies").header("Authorization","Bearer owner").header("Idempotency-Key","invalid-key").contentType("application/json").content(invalid)).andExpect(status().isBadRequest());
  mvc.perform(post("/api/v1/emergencies").header("Authorization","Bearer owner").contentType("application/json").content(body())).andExpect(status().isBadRequest());
  assertThat(alerts.count()).isZero();assertThat(outbox.count()).isZero();
 }
 @Test void filtersPagesAndRejectsInvalidTransitions() throws Exception {
  String id=json.readTree(create(body(),"filter-key")).get("id").asText();
  mvc.perform(get("/api/v1/emergencies?size=1&status=ACTIVE&type=PANIC").header("Authorization","Bearer owner")).andExpect(jsonPath("$.content[0].id").value(id));
  mvc.perform(get("/api/v1/emergencies?size=101").header("Authorization","Bearer owner")).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/v1/emergencies/"+id).header("Authorization","Bearer owner").contentType("application/json").content("{\"status\":\"RESOLVED\"}")).andExpect(status().isBadRequest());
  mvc.perform(get("/api/v1/emergencies/00000000-0000-4000-8000-000000000000").header("Authorization","Bearer owner")).andExpect(status().isNotFound());
 }
 @Test void staleLocationsFailWithoutPersistence() throws Exception {
  String stale=body().replaceAll("\"locationRecordedAt\":\"[^\"]+\"", "\"locationRecordedAt\":\"2020-01-01T00:00:00Z\"");
  mvc.perform(post("/api/v1/emergencies").header("Authorization","Bearer owner").header("Idempotency-Key","old-location").contentType("application/json").content(stale)).andExpect(status().isBadRequest());
  assertThat(alerts.count()).isZero();
 }
 @Test void unavailableAccountReturns503WithoutWriting() throws Exception {
  mvc.perform(post("/api/v1/emergencies").header("Authorization","Bearer unavailable").header("Idempotency-Key","unavailable-key").contentType("application/json").content(body())).andExpect(status().isServiceUnavailable());
  assertThat(alerts.count()).isZero();assertThat(outbox.count()).isZero();
 }
 @Test void rollsBackAlertAndRequestIfOutboxWriteFails() {
  org.mockito.Mockito.doThrow(new IllegalStateException("simulated outbox storage failure")).when(outbox).save(org.mockito.ArgumentMatchers.any(OutboxEvent.class));
  assertThatThrownBy(()->service.create("11111111-1111-4111-8111-111111111111","rollback-key",new EmergencyController.CreateAlert("PANIC",-12.0,-77.0,10.0,Instant.now()))).isInstanceOf(IllegalStateException.class);
  assertThat(alerts.count()).isZero();assertThat(requests.count()).isZero();assertThat(outbox.count()).isZero();
 }
 @Test void concurrentDuplicateRequestsCommitOneAlertAndOneEvent() throws Exception {
  String payload=body();var pool=Executors.newFixedThreadPool(4);var start=new CountDownLatch(1);
  try {
   var futures=new java.util.ArrayList<Future<Integer>>();
   for(int i=0;i<4;i++) futures.add(pool.submit(()->{start.await();return mvc.perform(post("/api/v1/emergencies").header("Authorization","Bearer owner").header("Idempotency-Key","concurrent-key").contentType("application/json").content(payload)).andReturn().getResponse().getStatus();}));
   start.countDown();for(var future:futures) assertThat(future.get(15,TimeUnit.SECONDS)).isIn(200,201);
  } finally {pool.shutdownNow();}
  assertThat(alerts.count()).isEqualTo(1);assertThat(requests.count()).isEqualTo(1);assertThat(outbox.count()).isEqualTo(1);
 }
}
