package com.uphill.infrastructure.security;

import com.uphill.core.auth.TokenProvider;
import com.uphill.core.domain.Admin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class JwtTokenProviderImpl implements TokenProvider {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}")
    private long jwtExpiration; // 24 hours in seconds

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public String generateToken(final Admin admin) {
        final Instant now = Instant.now();
        final Instant expiryDate = now.plus(jwtExpiration, ChronoUnit.SECONDS);
        
        return Jwts.builder()
                .subject(admin.getId().toString())
                .claim("role", "ADMIN")
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean validateToken(final String token) {
        log.debug("[JwtTokenProviderImpl] Validating token");
        
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (final JwtException | IllegalArgumentException e) {
            log.warn("[JwtTokenProviderImpl] Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Long getUserIdFromToken(final String token) {
        log.debug("[JwtTokenProviderImpl] Extracting user ID from token");
        
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            final String subject = claims.getSubject();
            return subject != null ? Long.parseLong(subject) : null;
        } catch (final JwtException | IllegalArgumentException e) {
            log.warn("[JwtTokenProviderImpl] Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }

}
