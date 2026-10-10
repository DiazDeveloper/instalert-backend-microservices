package com.instalert.account.infrastructure;

import com.instalert.account.domain.Account;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class AccountSecurity {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean SecurityFilterChain chain(HttpSecurity http, AccountTokens tokens, @Value("${account.cors.origins}") String origins) throws Exception {
        var cors=new CorsConfiguration(); cors.setAllowedOrigins(Arrays.asList(origins.split(",")));
        cors.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS")); cors.setAllowedHeaders(List.of("Authorization","Content-Type","X-Correlation-ID"));
        var source=new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",cors);
        return http.csrf(AbstractHttpConfigurer::disable).cors(c->c.configurationSource(source))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->res.sendError(401)).accessDeniedHandler((req,res,ex)->res.sendError(403)))
            .authorizeHttpRequests(a->a.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/api/v1/authentication/sign-up","/api/v1/authentication/sign-in").permitAll()
                .requestMatchers("/api/v1/users").hasRole("ADMIN").anyRequest().authenticated())
            .addFilterBefore(new OncePerRequestFilter() {
                @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
                    var header=req.getHeader("Authorization");
                    if(header!=null && header.startsWith("Bearer ")) {
                        try {
                            Account account=tokens.verify(header.substring(7));
                            var auth=new UsernamePasswordAuthenticationToken(account.accountId,null,account.identity.roles.stream().map(r->new SimpleGrantedAuthority(r.roleName)).toList());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        } catch(io.jsonwebtoken.JwtException | IllegalArgumentException invalid) { SecurityContextHolder.clearContext(); }
                        catch(org.springframework.dao.DataAccessException | org.springframework.transaction.TransactionException unavailable) {
                            res.sendError(503,"Account validation unavailable"); return;
                        }
                    }
                    chain.doFilter(req,res);
                }
            },UsernamePasswordAuthenticationFilter.class).build();
    }
}
