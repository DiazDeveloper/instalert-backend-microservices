package com.instalert_backend.iam.infrastructure.hashing.bcrypt;

import com.instalert_backend.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptHashingService extends BCryptPasswordEncoder implements HashingService {
}