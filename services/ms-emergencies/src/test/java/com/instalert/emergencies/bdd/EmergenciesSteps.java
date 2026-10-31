package com.instalert.emergencies.bdd;
import com.instalert.emergencies.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;
@CucumberContextConfiguration @SpringBootTest(classes=EmergenciesApplication.class)
public class EmergenciesSteps {
 @Autowired EmergencyService service;
 @Autowired EmergencyRepository alerts;
 @Autowired CreationRequestRepository requests;
 @Autowired OutboxRepository events;
 EmergencyController.CreateAlert request;
 ResponseStatusException rejection;
 @Before public void reset() {events.deleteAll();requests.deleteAll();alerts.deleteAll();rejection=null;}
 @Given("a recent valid emergency location") public void location() {request=new EmergencyController.CreateAlert("PANIC",-12.04,-77.04,10.0,Instant.now());}
 @When("the owner creates an emergency") public void create() {service.create("11111111-1111-4111-8111-111111111111","bdd-request",request);}
 @When("the owner repeats the same emergency request") public void retry() {create();create();}
 @Then("one active alert and its pending event are persisted") public void persisted() {
  assertThat(alerts.count()).isEqualTo(1);assertThat(alerts.findAll().getFirst().status).isEqualTo("ACTIVE");
  assertThat(events.count()).isEqualTo(1);assertThat(events.findAll().getFirst().publicationStatus).isEqualTo("PENDING");
 }
 @When("another account tries to cancel the owner's emergency") public void cancelOther() {
  create();try {service.cancel("22222222-2222-4222-8222-222222222222",alerts.findAll().getFirst().id);} catch(ResponseStatusException e) {rejection=e;}
 }
 @Then("access is forbidden and the alert remains active") public void forbidden() {assertThat(rejection).isNotNull();assertThat(rejection.getStatusCode().value()).isEqualTo(403);persisted();}
}
