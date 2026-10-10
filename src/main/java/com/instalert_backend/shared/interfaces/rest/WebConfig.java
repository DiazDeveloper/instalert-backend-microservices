package com.instalert_backend.shared.interfaces.rest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] origins;
    public WebConfig(@Value("${app.cors.origins:http://localhost:4200}") String origins) { this.origins=origins.split(","); }
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origins).allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
            .allowedHeaders("Authorization","Content-Type","X-Correlation-ID");
    }
}
