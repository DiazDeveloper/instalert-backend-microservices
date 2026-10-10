package com.instalert.gateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class GatewayCors implements WebMvcConfigurer {
    private final String[] origins;
    public GatewayCors(@Value("${gateway.cors-origins}") String origins) { this.origins=origins.split(","); }
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origins).allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
            .allowedHeaders("Authorization","Content-Type","X-Correlation-ID").exposedHeaders("X-Correlation-ID");
    }
}
