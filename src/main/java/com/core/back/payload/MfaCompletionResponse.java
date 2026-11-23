package com.core.back.payload;

import java.util.List;

public record MfaCompletionResponse(
        JwtResponse authentication,
        List<String> recoveryCodes) {
}
