package com.instalert.gateway;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;

class GatewayIntegrationTest {
    HttpServer account,monolith;
    GatewayController gateway;
    AtomicReference<String> authorization=new AtomicReference<>();
    @BeforeEach void start() throws Exception {
        account=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        monolith=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        account.createContext("/",exchange->{
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            if(exchange.getRequestURI().getPath().endsWith("empty-denied")) { exchange.sendResponseHeaders(403,-1); exchange.close(); return; }
            byte[] bytes=("account:"+exchange.getRequestURI()).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");
            exchange.sendResponseHeaders(exchange.getRequestURI().getPath().endsWith("denied")?403:200,bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        monolith.createContext("/",exchange->{
            byte[] bytes="monolith".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        });
        account.start(); monolith.start();
        gateway=new GatewayController("http://127.0.0.1:"+account.getAddress().getPort(),"http://127.0.0.1:"+monolith.getAddress().getPort());
    }
    @AfterEach void stop() { account.stop(0); monolith.stop(0); }
    MockHttpServletRequest request(String path) {
        var r=new MockHttpServletRequest("GET",path); r.addHeader("Authorization","Bearer test-token"); return r;
    }
    @Test void routesAccountAndRemainingContexts() {
        for(var path:new String[]{"/api/v1/users","/api/v1/authentication/sign-in","/api/v1/account/me"}) {
            var response=gateway.forward(request(path),null);
            assertThat(new String(response.getBody(),StandardCharsets.UTF_8)).startsWith("account:");
            assertThat(response.getHeaders().getFirst("X-Correlation-ID")).isNotBlank();
        }
        for(var path:new String[]{"/api/v1/incidents","/api/v1/emergencies","/api/v1/communities"})
            assertThat(new String(gateway.forward(request(path),null).getBody(),StandardCharsets.UTF_8)).isEqualTo("monolith");
        assertThat(authorization.get()).isEqualTo("Bearer test-token");
    }
    @Test void preserves403AndRejectsUnknownOrTraversalRoutes() {
        assertThat(gateway.forward(request("/api/v1/account/denied"),null).getStatusCode().value()).isEqualTo(403);
        assertThat(gateway.forward(request("/api/v1/account/empty-denied"),null).getStatusCode().value()).isEqualTo(403);
        assertThat(gateway.forward(request("/api/v1/users-evil"),null).getStatusCode().value()).isEqualTo(404);
        assertThat(gateway.forward(request("/api/v1/users/../incidents"),null).getStatusCode().value()).isEqualTo(400);
    }
    @Test void upstreamFailureReturns502() {
        account.stop(0);
        assertThat(gateway.forward(request("/api/v1/account/me"),null).getStatusCode().value()).isEqualTo(502);
    }
}
