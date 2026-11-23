package com.core.back.service.impl;

import com.core.back.payload.JwtResponse;
import com.core.back.payload.MfaChallengeResponse;
import com.core.back.payload.MfaCompletionResponse;
import com.core.back.repository.IMfaRepository;
import com.core.back.security.CustomUserDetails;
import com.core.back.security.MfaSecretCipher;
import com.core.back.security.TotpUtil;
import com.core.back.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class MfaService {

    private static final String CHALLENGE_KEY_PREFIX = "mfa:challenge:";
    private static final String PURPOSE_ENROLL = "ENROLL";
    private static final String PURPOSE_VERIFY = "VERIFY";
    private static final long CHALLENGE_LIFETIME_SECONDS = 300;
    private static final int RECOVERY_CODE_COUNT = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final IMfaRepository mfaRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MfaSecretCipher secretCipher;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final String issuer;

    public MfaService(
            IMfaRepository mfaRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            MfaSecretCipher secretCipher,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService,
            RefreshTokenService refreshTokenService,
            @Value("${app.security.mfa.issuer:Back}") String issuer) {
        this.mfaRepository = mfaRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.secretCipher = secretCipher;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.issuer = issuer;
    }

    public MfaChallengeResponse begin(CustomUserDetails userDetails) {
        Optional<String> encryptedSecret = mfaRepository.findEncryptedSecret(userDetails.getUserId());
        String purpose = encryptedSecret.isPresent() ? PURPOSE_VERIFY : PURPOSE_ENROLL;
        String enrollmentSecret = encryptedSecret.isEmpty() ? TotpUtil.generateSecret() : null;
        Challenge challenge = new Challenge(
                userDetails.getUserId(),
                userDetails.getUsername(),
                purpose,
                enrollmentSecret == null ? null : secretCipher.encrypt(enrollmentSecret));
        String challengeToken = createChallenge(challenge);

        return new MfaChallengeResponse(
                challengeToken,
                purpose.equals(PURPOSE_ENROLL) ? "ENROLL_TOTP" : "VERIFY_TOTP",
                CHALLENGE_LIFETIME_SECONDS,
                enrollmentSecret == null
                        ? null
                        : TotpUtil.provisioningUri(issuer, userDetails.getUsername(), enrollmentSecret),
                enrollmentSecret);
    }

    public MfaCompletionResponse confirmEnrollment(String challengeToken, String code) {
        Challenge challenge = findChallenge(challengeToken);
        if (!PURPOSE_ENROLL.equals(challenge.purpose()) || challenge.encryptedPendingSecret() == null) {
            throw invalidChallenge();
        }

        String secret = secretCipher.decrypt(challenge.encryptedPendingSecret());
        Long matchedStep = TotpUtil.matchingStep(secret, code, System.currentTimeMillis());
        if (matchedStep == null) {
            throw invalidCredentials();
        }
        CustomUserDetails userDetails = loadChallengeUser(challenge);
        List<String> recoveryCodes = generateRecoveryCodes();
        List<String> recoveryCodeHashes = recoveryCodes.stream().map(MfaService::hashRecoveryCode).toList();

        if (!consumeChallenge(challengeToken, challenge)) {
            throw invalidChallenge();
        }
        mfaRepository.completeEnrollment(
                userDetails.getUserId(),
                secretCipher.encrypt(secret),
                matchedStep,
                recoveryCodeHashes);
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = refreshTokenService.issue(userDetails);

        return new MfaCompletionResponse(
                new JwtResponse("Bearer", accessToken),
                recoveryCodes,
                refreshToken,
                refreshTokenService.getExpirationSeconds());
    }

    public MfaCompletionResponse verify(
            String challengeToken,
            String code,
            String recoveryCode) {
        Challenge challenge = findChallenge(challengeToken);
        if (!PURPOSE_VERIFY.equals(challenge.purpose())) {
            throw invalidChallenge();
        }

        CustomUserDetails userDetails = loadChallengeUser(challenge);
        boolean usedRecoveryCode = recoveryCode != null && !recoveryCode.isBlank();
        String recoveryCodeHash = null;
        if (usedRecoveryCode) {
            recoveryCodeHash = hashRecoveryCode(recoveryCode);
            if (!mfaRepository.isRecoveryCodeAvailable(userDetails.getUserId(), recoveryCodeHash)) {
                throw invalidCredentials();
            }
        } else {
            String encryptedSecret = mfaRepository.findEncryptedSecret(userDetails.getUserId())
                    .orElseThrow(MfaService::invalidCredentials);
            String secret = secretCipher.decrypt(encryptedSecret);
            Long matchedStep = TotpUtil.matchingStep(secret, code, System.currentTimeMillis());
            if (matchedStep == null || !mfaRepository.acceptTotpStep(userDetails.getUserId(), matchedStep)) {
                throw invalidCredentials();
            }
        }

        if (!consumeChallenge(challengeToken, challenge)) {
            throw invalidChallenge();
        }
        if (usedRecoveryCode
                && !mfaRepository.consumeRecoveryCode(userDetails.getUserId(), recoveryCodeHash)) {
            throw invalidCredentials();
        }

        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = refreshTokenService.issue(userDetails);
        return new MfaCompletionResponse(
                new JwtResponse("Bearer", accessToken),
                List.of(),
                refreshToken,
                refreshTokenService.getExpirationSeconds());
    }

    private String createChallenge(Challenge challenge) {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        try {
            redisTemplate.opsForValue().set(
                    challengeKey(token),
                    objectMapper.writeValueAsString(challenge),
                    Duration.ofSeconds(CHALLENGE_LIFETIME_SECONDS));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to create the MFA challenge", exception);
        }
        return token;
    }

    private Challenge findChallenge(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw invalidChallenge();
        }
        String value = redisTemplate.opsForValue().get(challengeKey(token));
        if (value == null) {
            throw invalidChallenge();
        }
        try {
            Challenge challenge = objectMapper.readValue(value, Challenge.class);
            if (challenge.userId() == null || challenge.username() == null || challenge.purpose() == null) {
                throw invalidChallenge();
            }
            return challenge;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to read the MFA challenge", exception);
        }
    }

    private boolean consumeChallenge(String token, Challenge expected) {
        String value = redisTemplate.opsForValue().getAndDelete(challengeKey(token));
        if (value == null) {
            return false;
        }
        try {
            return expected.equals(objectMapper.readValue(value, Challenge.class));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to read the MFA challenge", exception);
        }
    }

    private CustomUserDetails loadChallengeUser(Challenge challenge) {
        UserDetails loaded = userDetailsService.loadUserByUsername(challenge.username());
        if (!(loaded instanceof CustomUserDetails userDetails)
                || !userDetails.getUserId().equals(challenge.userId())
                || !userDetails.isEnabled()) {
            throw invalidCredentials();
        }
        return userDetails;
    }

    private List<String> generateRecoveryCodes() {
        List<String> codes = new ArrayList<>(RECOVERY_CODE_COUNT);
        for (int index = 0; index < RECOVERY_CODE_COUNT; index++) {
            byte[] random = new byte[16];
            SECURE_RANDOM.nextBytes(random);
            String value = HexFormat.of().withUpperCase().formatHex(random);
            codes.add(value.substring(0, 8) + "-" + value.substring(8, 16)
                    + "-" + value.substring(16, 24) + "-" + value.substring(24));
        }
        return List.copyOf(codes);
    }

    private static String hashRecoveryCode(String recoveryCode) {
        String normalized = recoveryCode.replace("-", "").trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-F0-9]{32}")) {
            throw invalidCredentials();
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static String challengeKey(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII));
            return CHALLENGE_KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static BadCredentialsException invalidChallenge() {
        return new BadCredentialsException("The MFA challenge is invalid or expired.");
    }

    private static BadCredentialsException invalidCredentials() {
        return new BadCredentialsException("Invalid credentials");
    }

    private record Challenge(
            Long userId,
            String username,
            String purpose,
            String encryptedPendingSecret) {
    }
}
