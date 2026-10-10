package com.instalert.account.bdd;
import com.fasterxml.jackson.databind.*;
import com.instalert.account.AccountApplication;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@CucumberContextConfiguration @SpringBootTest(classes=AccountApplication.class) @AutoConfigureMockMvc
public class AccountSteps {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json;
    JsonNode first,second; MvcResult response;
    JsonNode register() throws Exception {
        response=mvc.perform(post("/api/v1/authentication/sign-up").contentType("application/json")
            .content(json.writeValueAsString(Map.of("name","BDD Citizen","email",UUID.randomUUID()+"@example.org","password","Passw0rd!"))))
            .andReturn();
        assertThat(response.getResponse().getStatus()).isEqualTo(201);
        return json.readTree(response.getResponse().getContentAsString());
    }
    @When("a citizen registers a complete account") public void registration() throws Exception { first=register(); }
    @Then("registration succeeds with an associated profile and citizen authority") public void associated() {
        assertThat(first.get("profile").get("accountId").asText()).isEqualTo(first.get("accountId").asText());
        assertThat(first.get("profile").get("roles").get(0).asText()).isEqualTo("ROLE_USER");
    }
    @Given("a registered citizen") public void citizen() throws Exception { first=register(); }
    @When("the citizen signs in with an incorrect password") public void wrongPassword() throws Exception {
        response=mvc.perform(post("/api/v1/authentication/sign-in").contentType("application/json")
            .content(json.writeValueAsString(Map.of("email",first.get("email").asText(),"password","wrong")))).andReturn();
    }
    @Then("the request is rejected with status {int}") public void rejected(int code) { assertThat(response.getResponse().getStatus()).isEqualTo(code); }
    @Given("two registered citizens") public void two() throws Exception { first=register();second=register(); }
    @When("the first citizen edits the second citizen profile") public void editOther() throws Exception {
        response=mvc.perform(put("/api/v1/users/"+second.get("accountId").asText()).header("Authorization","Bearer "+first.get("token").asText())
            .contentType("application/json").content("{\"email\":\"changed@example.org\"}")).andReturn();
    }
    @Then("the second citizen email remains unchanged") public void unchanged() throws Exception {
        var result=mvc.perform(get("/api/v1/account/me").header("Authorization","Bearer "+second.get("token").asText())).andReturn();
        assertThat(json.readTree(result.getResponse().getContentAsString()).get("email").asText()).isEqualTo(second.get("email").asText());
    }
}
