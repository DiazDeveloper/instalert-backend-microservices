package com.instalert_backend.iam.infrastructure.authorization.sfs.configuration;

import com.instalert_backend.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.instalert_backend.iam.infrastructure.authorization.sfs.pipeline.UnauthorizedRequestHandlerEntryPoint;
import com.instalert_backend.iam.infrastructure.authorization.sfs.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private final UserDetailsServiceImpl userDetailsService;
    private final BearerAuthorizationRequestFilter bearerAuthorizationRequestFilter;
    private final UnauthorizedRequestHandlerEntryPoint unauthorizedRequestHandlerEntryPoint;
    private final PasswordEncoder passwordEncoder;

    public WebSecurityConfiguration(UserDetailsServiceImpl userDetailsService,
                                    BearerAuthorizationRequestFilter bearerAuthorizationRequestFilter,
                                    UnauthorizedRequestHandlerEntryPoint unauthorizedRequestHandlerEntryPoint,
                                    PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.bearerAuthorizationRequestFilter = bearerAuthorizationRequestFilter;
        this.unauthorizedRequestHandlerEntryPoint = unauthorizedRequestHandlerEntryPoint;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedRequestHandlerEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Publico: login, registro, cambio de contraseña con validacion propia
                        .requestMatchers("/api/v1/authentication/**").permitAll()
                        // Publico: documentacion
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Publico: crear cuenta de perfil justo despues del registro en iam
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/users").permitAll()
                        // Publico: ver listados (util para la demo y para no romper flujos existentes)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/users/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/incidents/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/emergencies/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/communities/**").permitAll()
                        // Todo lo demas requiere estar autenticado con token valido
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(bearerAuthorizationRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}