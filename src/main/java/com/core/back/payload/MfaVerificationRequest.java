package com.core.back.payload;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaVerificationRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}") String challengeToken,
        @Pattern(regexp = "\\d{6}") String code,
        @Pattern(regexp = "(?:[A-Fa-f0-9]{32}|[A-Fa-f0-9]{8}(?:-[A-Fa-f0-9]{8}){3})")
        String recoveryCode) {

    @AssertTrue(message = "Provide either a TOTP code or a recovery code, but not both.")
    public boolean hasExactlyOneCode() {
        return (code != null && !code.isBlank()) != (recoveryCode != null && !recoveryCode.isBlank());
    }
}
