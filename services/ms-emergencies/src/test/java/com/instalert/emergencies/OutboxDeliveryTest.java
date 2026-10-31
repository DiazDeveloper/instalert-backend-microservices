package com.instalert.emergencies;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest
class OutboxDeliveryTest {
 @Autowired EmergencyService service;@Autowired OutboxRepository outbox;@Autowired CreationRequestRepository requests;@Autowired EmergencyRepository alerts;@Autowired ObjectMapper json;
 @BeforeEach void clear(){outbox.deleteAll();requests.deleteAll();alerts.deleteAll();}
 @Test void retriesAfterUnavailableReceiverWithSameEventId() throws Exception {
  var codes=new AtomicInteger(503);var calls=new java.util.ArrayList<String>();
  var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/api/v1/communities/internal/emergency-events",e->{calls.add(new String(e.getRequestBody().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));e.sendResponseHeaders(codes.get(),-1);e.close();});server.start();
  try{
   service.create("11111111-1111-4111-8111-111111111111","delivery-test",new EmergencyController.CreateAlert("PANIC",-12.,-77.,10.,Instant.now()));
   // Use a transactional proxy so updates commit exactly as with the scheduled worker.
   var tx=new org.springframework.transaction.support.TransactionTemplate(manager);
   var delivery=new NotificationDelivery(outbox,json,"http://127.0.0.1:"+server.getAddress().getPort(),"a".repeat(64));
   tx.executeWithoutResult(s->delivery.deliver());assertThat(outbox.findAll().getFirst().publicationStatus).isEqualTo("PENDING");
   codes.set(204);tx.executeWithoutResult(s->delivery.deliver());var event=outbox.findAll().getFirst();
   assertThat(event.publicationStatus).isEqualTo("PUBLISHED");assertThat(event.deliveryAttempts).isEqualTo(2);assertThat(event.publishedAt).isNotNull();assertThat(calls).hasSize(2);assertThat(calls.get(0)).isEqualTo(calls.get(1));
  }finally{server.stop(0);}
 }
 @Autowired org.springframework.transaction.PlatformTransactionManager manager;
}
