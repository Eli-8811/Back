package com.core.back.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LoginRateLimiterService {

    private static final String KEY_PREFIX = "ratelimit:login:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Pattern IPV4_PATTERN = Pattern.compile("(?:\\d{1,3}\\.){3}\\d{1,3}");
    private static final Pattern IPV6_PATTERN = Pattern.compile("[0-9a-fA-F:.]+");
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
            local attempts = redis.call('INCR', KEYS[1])
            if attempts == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return attempts
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final Set<String> trustedProxies;

    public LoginRateLimiterService(
            StringRedisTemplate redisTemplate,
            @Value("${app.security.trusted-proxies:}") List<String> trustedProxyAddresses) {
        this.redisTemplate = redisTemplate;
        this.trustedProxies = trustedProxyAddresses.stream()
                .map(String::trim)
                .filter(address -> !address.isEmpty())
                .map(LoginRateLimiterService::normalizeIpLiteral)
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAllowed(HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        Long attempts = redisTemplate.execute(
                INCREMENT_SCRIPT,
                List.of(KEY_PREFIX + clientIp),
                Long.toString(WINDOW.toSeconds()));
        if (attempts == null) {
            throw new IllegalStateException("Redis did not return a login attempt count");
        }
        return attempts <= MAX_ATTEMPTS;
    }

    public void reset(HttpServletRequest request) {
        redisTemplate.delete(KEY_PREFIX + resolveClientIp(request));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddress = normalizeIpLiteral(request.getRemoteAddr());
        if (!trustedProxies.contains(remoteAddress)) {
            return remoteAddress;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return remoteAddress;
        }

        List<String> forwardedAddresses = Arrays.stream(forwardedFor.split(","))
                .map(String::trim)
                .filter(address -> !address.isEmpty())
                .map(LoginRateLimiterService::normalizeIpLiteral)
                .toList();

        for (int index = forwardedAddresses.size() - 1; index >= 0; index--) {
            String address = forwardedAddresses.get(index);
            if (!trustedProxies.contains(address)) {
                return address;
            }
        }
        return forwardedAddresses.isEmpty() ? remoteAddress : forwardedAddresses.getFirst();
    }

    private static String normalizeIpLiteral(String value) {
        if (value == null || !(IPV4_PATTERN.matcher(value).matches()
                || (value.contains(":") && IPV6_PATTERN.matcher(value).matches()))) {
            throw new IllegalArgumentException("Invalid IP address in request or trusted proxy configuration");
        }
        try {
            return InetAddress.getByName(value).getHostAddress();
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("Invalid IP address in request or trusted proxy configuration", exception);
        }
    }
}
