package com.core.back.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class MfaSecretCipher {

    private static final int IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    public MfaSecretCipher(@Value("${app.security.mfa.encryption-key}") String encodedKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            if (keyBytes.length != 32) {
                throw new IllegalStateException("MFA encryption key must decode to exactly 32 bytes");
            }
            this.key = new SecretKeySpec(keyBytes, "AES");
            Arrays.fill(keyBytes, (byte) 0);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("MFA encryption key must be Base64-encoded", exception);
        }
    }

    public String encrypt(String plaintext) {
        byte[] iv = new byte[IV_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        byte[] ciphertext = crypt(Cipher.ENCRYPT_MODE, plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8), iv);
        byte[] encrypted = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedValue) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedValue);
            if (encrypted.length <= IV_BYTES) {
                throw new IllegalArgumentException("Encrypted MFA secret is incomplete");
            }
            byte[] iv = Arrays.copyOfRange(encrypted, 0, IV_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(encrypted, IV_BYTES, encrypted.length);
            return new String(crypt(Cipher.DECRYPT_MODE, ciphertext, iv), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Encrypted MFA secret is invalid", exception);
        }
    }

    private byte[] crypt(int mode, byte[] input, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(input);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to encrypt or decrypt MFA secret", exception);
        }
    }
}
