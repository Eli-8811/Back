package com.core.back.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MfaSecretCipherTest {

    @Test
    void encryptsAndDecryptsSecret() {
        String key = Base64.getEncoder().encodeToString(new byte[32]);
        MfaSecretCipher cipher = new MfaSecretCipher(key);

        String encrypted = cipher.encrypt("authenticator-secret");

        assertEquals("authenticator-secret", cipher.decrypt(encrypted));
    }

    @Test
    void rejectsTamperedCiphertext() {
        String key = Base64.getEncoder().encodeToString(new byte[32]);
        MfaSecretCipher cipher = new MfaSecretCipher(key);
        String encrypted = cipher.encrypt("authenticator-secret");
        byte[] ciphertext = Base64.getDecoder().decode(encrypted);
        ciphertext[ciphertext.length - 1] ^= 1;

        assertThrows(IllegalStateException.class,
                () -> cipher.decrypt(Base64.getEncoder().encodeToString(ciphertext)));
    }
}
