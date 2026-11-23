package com.core.back.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

public final class TotpUtil {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_BYTES = 20;
    private static final int CODE_DIGITS = 6;
    private static final long TIME_STEP_SECONDS = 30;
    private static final int ALLOWED_CLOCK_SKEW_STEPS = 1;
    private static final long TOTP_MODULUS = 1_000_000L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TotpUtil() {
    }

    public static String generateSecret() {
        byte[] secret = new byte[SECRET_BYTES];
        SECURE_RANDOM.nextBytes(secret);
        return encodeBase32(secret);
    }

    public static boolean verify(String secret, String code, long epochMillis) {
        return matchingStep(secret, code, epochMillis) != null;
    }

    public static Long matchingStep(String secret, String code, long epochMillis) {
        if (code == null || !code.matches("\\d{" + CODE_DIGITS + "}")) {
            return null;
        }

        byte[] key = decodeBase32(secret);
        long currentStep = Math.floorDiv(epochMillis / 1000, TIME_STEP_SECONDS);
        byte[] candidate = code.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        for (long offset = -ALLOWED_CLOCK_SKEW_STEPS; offset <= ALLOWED_CLOCK_SKEW_STEPS; offset++) {
            long step = currentStep + offset;
            byte[] expected = generateCode(key, step)
                    .getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            if (MessageDigest.isEqual(expected, candidate)) {
                return step;
            }
        }
        return null;
    }

    public static String provisioningUri(String issuer, String username, String secret) {
        String label = urlEncode(issuer) + ":" + urlEncode(username);
        return "otpauth://totp/" + label + "?secret=" + urlEncode(secret)
                + "&issuer=" + urlEncode(issuer) + "&algorithm=SHA1&digits="
                + CODE_DIGITS + "&period=" + TIME_STEP_SECONDS;
    }

    private static String generateCode(byte[] key, long timeStep) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(timeStep).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            return String.format(Locale.ROOT, "%06d", binary % TOTP_MODULUS);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to generate a TOTP code", exception);
        }
    }

    private static String encodeBase32(byte[] bytes) {
        StringBuilder encoded = new StringBuilder((bytes.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsInBuffer = 0;
        for (byte value : bytes) {
            buffer = (buffer << 8) | (value & 0xFF);
            bitsInBuffer += 8;
            while (bitsInBuffer >= 5) {
                encoded.append(BASE32_ALPHABET.charAt((buffer >> (bitsInBuffer - 5)) & 0x1F));
                bitsInBuffer -= 5;
            }
        }
        if (bitsInBuffer > 0) {
            encoded.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsInBuffer)) & 0x1F));
        }
        return encoded.toString();
    }

    private static byte[] decodeBase32(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalArgumentException("TOTP secret must not be blank");
        }
        String normalized = encoded.replace("=", "").toUpperCase(Locale.ROOT);
        byte[] decoded = new byte[normalized.length() * 5 / 8];
        int buffer = 0;
        int bitsInBuffer = 0;
        int index = 0;
        for (char character : normalized.toCharArray()) {
            int value = BASE32_ALPHABET.indexOf(character);
            if (value < 0) {
                throw new IllegalArgumentException("TOTP secret is not valid Base32");
            }
            buffer = (buffer << 5) | value;
            bitsInBuffer += 5;
            if (bitsInBuffer >= 8) {
                decoded[index++] = (byte) (buffer >> (bitsInBuffer - 8));
                bitsInBuffer -= 8;
            }
        }
        if (index != decoded.length || index == 0) {
            throw new IllegalArgumentException("TOTP secret is not valid Base32");
        }
        return decoded;
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}
