package com.core.back.utils;

import com.core.back.security.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final JwtParser parser;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        this.expiration = expiration;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parser()
                .verifyWith(key)
                .build();
    }

    public String generateToken(CustomUserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUserId().toString())
                .claim("username", userDetails.getUsername())
                .claim("roles", userDetails.getRoles())
                .claim("permissions", userDetails.getPermissions())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public TokenClaims parseToken(String token) {
        Claims claims = getClaims(token);
        String subject = claims.getSubject();
        String username = claims.get("username", String.class);

        if (subject == null || username == null || username.isBlank()) {
            throw new IllegalArgumentException("JWT must contain a subject and username");
        }

        try {
            Long userId = Long.valueOf(subject);
            return new TokenClaims(
                    userId,
                    username,
                    readStringListClaim(claims, "roles"),
                    readStringListClaim(claims, "permissions"));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("JWT subject must be a numeric user ID", exception);
        }
    }

    private List<String> readStringListClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (!(value instanceof List<?> values)
                || values.stream().anyMatch(item -> !(item instanceof String))) {
            throw new IllegalArgumentException("JWT claim must be a list of strings: " + claimName);
        }
        return values.stream()
                .map(String.class::cast)
                .toList();
    }

    private Claims getClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    public record TokenClaims(Long userId, String username, List<String> roles, List<String> permissions) {
    }
}