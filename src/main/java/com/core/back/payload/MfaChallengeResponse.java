package com.core.back.payload;

public record MfaChallengeResponse(
        String challengeToken,
        String nextStep,
        long expiresInSeconds,
        String provisioningUri,
        String manualEntryKey) {
}
