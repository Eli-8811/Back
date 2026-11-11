package com.core.back.payload;

import java.time.Instant;

public record SecurityErrorDetails(
        Instant timestamp,
        int status,
        String error,
        String path) {
}
