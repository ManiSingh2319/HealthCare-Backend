package com.healthcare.security;

import com.healthcare.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtConfig jwtConfig;
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public String generateAccessToken(Long userId, String subject) {
        return generateToken(userId, subject, jwtConfig.getAccessTokenExpirationMs(), "access");
    }

    public String generateRefreshToken(Long userId, String subject) {
        return generateToken(userId, subject, jwtConfig.getRefreshTokenExpirationMs(), "refresh");
    }

    private String generateToken(Long userId, String subject, long expiryMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);
        return Jwts.builder()
                .subject(subject)
                .claims(Map.of("userId", userId, "type", type))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key())
                .compact();
    }

    public boolean isValid(String token) {
        try {
            return !blacklistedTokens.contains(token) && getClaims(token).getExpiration().after(new Date());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getClaims(token).get("type", String.class));
    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public void blacklist(String token) {
        blacklistedTokens.add(token);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
