package com.core.back.service.impl;

import com.core.back.security.CustomUserDetails;
import com.core.back.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RefreshTokenService {

    private static final String KEY_PREFIX = "jwt:refresh:";
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "^([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\\.([A-Za-z0-9_-]{43})$");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            local ttl = redis.call('TTL', KEYS[1])
            if redis.call('GET', KEYS[1]) == ARGV[1] and ttl > 0 then
                redis.call('SET', KEYS[1], ARGV[2], 'EX', ttl)
                return ttl
            end
            redis.call('DEL', KEYS[1])
            return 0
            """, Long.class);
    private static final DefaultRedisScript<Long> REVOKE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final long expirationSeconds;

    public RefreshTokenService(
            StringRedisTemplate redisTemplate,
            UserDetailsService userDetailsService,
            JwtUtil jwtUtil,
            @Value("${jwt.refresh.expiration-seconds:2592000}") long expirationSeconds) {
        if (expirationSeconds <= 0) {
            throw new IllegalStateException("jwt.refresh.expiration-seconds must be greater than zero");
        }
        this.redisTemplate = redisTemplate;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.expirationSeconds = expirationSeconds;
    }

    public String issue(CustomUserDetails userDetails) {
        String sessionId = UUID.randomUUID().toString();
        String secret = generateSecret();
        String token = sessionId + "." + secret;
        redisTemplate.opsForValue().set(
                sessionKey(sessionId),
                tokenRecord(secret, userDetails.getUserId(), userDetails.getUsername()),
                Duration.ofSeconds(expirationSeconds));
        return token;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public RotatedTokens rotate(String refreshToken) {
        ParsedToken parsed = parse(refreshToken);
        String key = sessionKey(parsed.sessionId());
        String currentRecord = redisTemplate.opsForValue().get(key);
        if (currentRecord == null) {
            throw invalidRefreshToken();
        }

        StoredSession session = parseRecord(currentRecord);
        String expectedRecord = tokenRecord(parsed.secret(), session.userId(), session.username());
        UserDetails loaded = userDetailsService.loadUserByUsername(session.username());
        if (!(loaded instanceof CustomUserDetails userDetails)
                || !userDetails.isEnabled()
                || !session.userId().equals(userDetails.getUserId())) {
            redisTemplate.delete(key);
            throw invalidRefreshToken();
        }

        String accessToken = jwtUtil.generateToken(userDetails);
        String nextSecret = generateSecret();
        String nextRefreshToken = parsed.sessionId() + "." + nextSecret;
        Long rotated = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(key),
                expectedRecord,
                tokenRecord(nextSecret, userDetails.getUserId(), userDetails.getUsername()));
        if (rotated == null || rotated <= 0) {
            throw invalidRefreshToken();
        }

        return new RotatedTokens(
                accessToken,
                nextRefreshToken,
                rotated);
    }

    public void revoke(String refreshToken) {
        ParsedToken parsed = parse(refreshToken);
        String key = sessionKey(parsed.sessionId());
        String currentRecord = redisTemplate.opsForValue().get(key);
        if (currentRecord != null) {
            StoredSession session = parseRecord(currentRecord);
            String expectedRecord = tokenRecord(parsed.secret(), session.userId(), session.username());
            redisTemplate.execute(REVOKE_SCRIPT, List.of(key), expectedRecord);
        }
    }

    private ParsedToken parse(String token) {
        if (token == null) {
            throw invalidRefreshToken();
        }
        Matcher matcher = TOKEN_PATTERN.matcher(token);
        if (!matcher.matches()) {
            throw invalidRefreshToken();
        }
        return new ParsedToken(matcher.group(1), matcher.group(2));
    }

    private static String tokenRecord(String secret, Long userId, String username) {
        return digest(secret) + ":"
                + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(username.getBytes(StandardCharsets.UTF_8))
                + ":" + userId;
    }

    private static StoredSession parseRecord(String record) {
        int separator = record.indexOf(':');
        int idSeparator = record.lastIndexOf(':');
        if (separator <= 0 || idSeparator <= separator || idSeparator == record.length() - 1) {
            throw invalidRefreshToken();
        }
        try {
            String username = new String(
                    Base64.getUrlDecoder().decode(record.substring(separator + 1, idSeparator)),
                    StandardCharsets.UTF_8);
            Long userId = Long.valueOf(record.substring(idSeparator + 1));
            if (username.isBlank()) {
                throw invalidRefreshToken();
            }
            return new StoredSession(username, userId);
        } catch (IllegalArgumentException exception) {
            throw invalidRefreshToken();
        }
    }

    private static String digest(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static String generateSecret() {
        byte[] secret = new byte[32];
        SECURE_RANDOM.nextBytes(secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }

    private static String sessionKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private static BadCredentialsException invalidRefreshToken() {
        return new BadCredentialsException("The refresh token is invalid, expired, or revoked.");
    }

    private record ParsedToken(String sessionId, String secret) {
    }

    private record StoredSession(String username, Long userId) {
    }

    public record RotatedTokens(
            String accessToken,
            String refreshToken,
            long refreshTokenExpiresInSeconds) {
    }
}
