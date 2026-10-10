package com.instalert_backend.iam.application.internal.outboundservices.tokens;

import org.springframework.security.core.userdetails.UserDetails;

public interface TokenService {
    String generateToken(String email);
    String getEmailFromToken(String token);
    boolean validateToken(String token);
}