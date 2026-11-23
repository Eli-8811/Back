package com.core.back.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaEnrollmentConfirmationRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}") String challengeToken,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Code must contain exactly 6 digits.") String code) {
}
