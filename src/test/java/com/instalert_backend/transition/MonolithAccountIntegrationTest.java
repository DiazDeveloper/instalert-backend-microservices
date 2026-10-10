package com.instalert_backend.transition;

import com.sun.net.httpserver.HttpServer;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class MonolithAccountIntegrationTest {
    static HttpServer account;
    static {
        try {
            account=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            account.createContext("/api/v1/account/session",exchange->{
                String token=exchange.getRequestHeaders().getFirst("Authorization");
                if(!"Bearer valid".equals(token)) { exchange.sendResponseHeaders(401,-1); exchange.close(); return; }
                byte[] body="{\"accountId\":\"public-uuid\",\"referenceId\":42,\"roles\":[\"ROLE_USER\"]}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type","application/json");
                exchange.sendResponseHeaders(200,body.length); exchange.getResponseBody().write(body); exchange.close();
            });
            account.start();
        } catch(Exception e) { throw new ExceptionInInitializerError(e); }
    }
    @DynamicPropertySource static void configure(DynamicPropertyRegistry registry) {
        registry.add("account.service.url",()->"http://127.0.0.1:"+account.getAddress().getPort());
    }
    @AfterAll static void stop() { account.stop(0); }
    @Autowired MockMvc mvc;
    @Autowired EntityManagerFactory emf;
    @Autowired ApplicationContext context;
    @Test void ownsOnlyRemainingContextEntitiesAndNoIamServices() {
        var names=emf.getMetamodel().getEntities().stream().map(e->e.getJavaType().getName()).toList();
        assertThat(names).hasSize(3).noneMatch(n->n.contains(".iam.") || n.contains(".profiles."));
        assertThat(context.getBeanNamesForType(com.instalert_backend.iam.application.commandservices.UserCommandService.class)).isEmpty();
    }
    @Test void resolvesSessionFromAccountAndRejectsLegacyRoutes() throws Exception {
        mvc.perform(get("/api/v1/incidents")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/incidents").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/incidents").header("Authorization","Bearer valid")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/users").header("Authorization","Bearer valid")).andExpect(status().isForbidden());
    }
    @Test void cannotCreateReferencesForAnotherAccount() throws Exception {
        mvc.perform(post("/api/v1/communities").header("Authorization","Bearer valid").contentType("application/json")
            .content("{\"name\":\"Test\",\"description\":\"Test\",\"isPrivate\":false,\"ownerId\":1}"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/communities").header("Authorization","Bearer valid"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }
}
