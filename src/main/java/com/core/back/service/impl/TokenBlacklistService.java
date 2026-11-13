package com.core.back.service.impl;

import com.core.back.service.ITokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService implements ITokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private static final String KEY_PREFIX = "jwt:blacklist:";
    private static final String REVOKED_VALUE = "revoked";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String tokenId, long remainingExpirationMillis) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException("Token ID must not be blank");
        }
        if (remainingExpirationMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(
                KEY_PREFIX + tokenId,
                REVOKED_VALUE,
                Duration.ofMillis(remainingExpirationMillis));
        log.info("JWT revoked and added to blacklist");
    }

    @Override
    public boolean isTokenBlacklisted(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + tokenId));
    }

}
