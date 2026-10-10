package com.instalert_backend.transition;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestClient;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Transition adapter: only Account can resolve identities; no local IAM table access. */
@Configuration
public class AccountSecurityConfiguration {
    public record AccountPrincipal(String accountId,long referenceId,List<String> roles) {}
    @Bean SecurityFilterChain accountSecurity(HttpSecurity http,
        @Value("${account.service.url:http://localhost:8081}") String accountUrl,
        @Value("${app.cors.origins:http://localhost:4200}") String origins) throws Exception {
        var factory=new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(2000); factory.setReadTimeout(3000);
        var client=RestClient.builder().baseUrl(accountUrl).requestFactory(factory).build();
        var cors=new CorsConfiguration(); cors.setAllowedOrigins(Arrays.asList(origins.split(",")));
        cors.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization","Content-Type","X-Correlation-ID"));
        var source=new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",cors);
        return http.csrf(AbstractHttpConfigurer::disable).cors(c->c.configurationSource(source))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->res.sendError(401)).accessDeniedHandler((req,res,ex)->res.sendError(403)))
            .authorizeHttpRequests(a->a.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/api/v1/users/**","/api/v1/authentication/**","/api/v1/account/**").denyAll()
                .requestMatchers("/swagger-ui/**","/v3/api-docs/**").permitAll().anyRequest().authenticated())
            .addFilterBefore(new OncePerRequestFilter() {
                @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
                    String token=req.getHeader("Authorization");
                    if(token!=null && token.startsWith("Bearer ")) {
                        try {
                            var principal=client.get().uri("/api/v1/account/session").header("Authorization",token).retrieve().body(AccountPrincipal.class);
                            if(principal!=null && principal.accountId()!=null && principal.referenceId()>0 && principal.roles()!=null) {
                                var auth=new UsernamePasswordAuthenticationToken(principal,null,principal.roles().stream().map(SimpleGrantedAuthority::new).toList());
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                        } catch(org.springframework.web.client.HttpClientErrorException e) {
                            SecurityContextHolder.clearContext();
                        } catch(org.springframework.web.client.RestClientException e) {
                            res.sendError(503,"Account validation unavailable"); return;
                        }
                    }
                    chain.doFilter(req,res);
                }
            },UsernamePasswordAuthenticationFilter.class).build();
    }
}
