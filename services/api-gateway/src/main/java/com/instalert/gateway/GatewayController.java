package com.instalert.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** Fixed Strangler routes, no user-controlled destination or forwarding of identity headers. */
@RestController
public class GatewayController {
    private final RestClient account,monolith;
    private static final Pattern ACCOUNT=Pattern.compile("^/api/v1/(authentication|users|account)(/.*)?$");
    private static final Pattern REMAINING=Pattern.compile("^/api/v1/(emergencies|incidents|communities)(/.*)?$");
    private static final Set<String> RESPONSE_HEADERS=Set.of("Content-Type","Location","ETag","Cache-Control");
    public GatewayController(@Value("${gateway.account-url}") String accountUrl,@Value("${gateway.monolith-url}") String monolithUrl) {
        var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).followRedirects(HttpClient.Redirect.NEVER).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        account=RestClient.builder().baseUrl(accountUrl).requestFactory(factory).build();
        monolith=RestClient.builder().baseUrl(monolithUrl).requestFactory(factory).build();
    }
    public static boolean routesToAccount(String path) { return ACCOUNT.matcher(path).matches(); }
    @RequestMapping("/api/v1/**")
    public ResponseEntity<byte[]> forward(HttpServletRequest request,@RequestBody(required=false) byte[] body) {
        var path=request.getRequestURI();
        if(!routesToAccount(path) && !REMAINING.matcher(path).matches()) return ResponseEntity.notFound().build();
        if(path.contains("..") || path.contains("\\") || path.contains("%")) return ResponseEntity.badRequest().build();
        String correlation=request.getHeader("X-Correlation-ID");
        if(correlation==null || !correlation.matches("[a-zA-Z0-9_-]{1,64}")) correlation=UUID.randomUUID().toString();
        final String correlationId=correlation;
        var uri=path+(request.getQueryString()==null?"":"?"+request.getQueryString());
        try {
            var outgoing=(routesToAccount(path)?account:monolith).method(HttpMethod.valueOf(request.getMethod())).uri(uri)
                .header("X-Correlation-ID",correlationId);
            for(String name:Set.of("Authorization","Content-Type","Accept")) {
                var value=request.getHeader(name); if(value!=null) outgoing.header(name,value);
            }
            if(body!=null && body.length>0) outgoing.body(body);
            return outgoing.exchange((req,res)->{
                var headers=new HttpHeaders();
                RESPONSE_HEADERS.forEach(name->{ var values=res.getHeaders().get(name); if(values!=null) headers.put(name,values); });
                headers.set("X-Correlation-ID",correlationId);
                return new ResponseEntity<>(res.getBody().readAllBytes(),headers,res.getStatusCode());
            });
        } catch(RestClientException e) { return ResponseEntity.status(502).header("X-Correlation-ID",correlationId).build(); }
    }
}
