package com.core.back.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpUtilTest {

    @Test
    void verifiesRfc6238SixDigitCode() {
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

        assertTrue(TotpUtil.verify(secret, "287082", 59_000));
        assertFalse(TotpUtil.verify(secret, "287083", 59_000));
        assertFalse(TotpUtil.verify(secret, "287082x", 59_000));
    }

    @Test
    void generatedSecretCanBeUsedToBuildAuthenticatorUri() {
        String secret = TotpUtil.generateSecret();
        String uri = TotpUtil.provisioningUri("Back", "test user", secret);

        assertTrue(uri.startsWith("otpauth://totp/Back:test%20user?"));
        assertTrue(uri.contains("secret=" + secret));
        assertTrue(uri.contains("digits=6&period=30"));
    }
}
