package com.instalert.account.infrastructure;

import com.instalert.account.domain.Account;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class AccountTokens {
    private final Key key; private final long ttl; private final AccountRepository accounts;
    public AccountTokens(@Value("${account.jwt.secret}") String secret, @Value("${account.jwt.ttl-seconds}") long ttl, AccountRepository accounts) {
        key=Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        if(ttl<60 || ttl>86400) throw new IllegalArgumentException("JWT TTL must be between 60 and 86400 seconds");
        this.ttl=ttl; this.accounts=accounts;
    }
    public String issue(Account account) {
        var now=Instant.now();
        return Jwts.builder().setSubject(account.accountId).setIssuer("ms-account").setAudience("instalert-api")
            .setIssuedAt(Date.from(now)).setExpiration(Date.from(now.plusSeconds(ttl)))
            .claim("ver",account.identity.tokenVersion).signWith(key,SignatureAlgorithm.HS256).compact();
    }
    @Transactional(readOnly=true)
    public Account verify(String token) {
        var jwt=Jwts.parserBuilder().setSigningKey(key).requireIssuer("ms-account").requireAudience("instalert-api").build().parseClaimsJws(token);
        if(!"HS256".equals(jwt.getHeader().getAlgorithm()) || jwt.getBody().getExpiration()==null) throw new JwtException("Invalid token");
        var a=accounts.findById(jwt.getBody().getSubject()).orElseThrow(()->new JwtException("Invalid token"));
        var version=jwt.getBody().get("ver",Number.class);
        if(!a.isActive() || version==null || version.longValue()!=a.identity.tokenVersion) throw new JwtException("Invalid token");
        return a;
    }
}
