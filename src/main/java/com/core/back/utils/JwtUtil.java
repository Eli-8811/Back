package com.core.back.utils;

import com.core.back.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    private static final int MIN_RSA_KEY_SIZE = 2048;

    private final PrivateKey signingKey;
    private final String signingKeyId;
    private final Map<String, PublicKey> verificationKeys;
    private final JwtParser parser;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.signing.key-id:2026-01}") String signingKeyId,
            @Value("${jwt.signing.private-key-location:file:C:/secure/jwt/private-key.pem}")
            String privateKeyLocation,
            @Value("${jwt.verification-keys:2026-01=file:C:/secure/jwt/public-key.pem}")
            String verificationKeyLocations,
            @Value("${jwt.expiration}") long expiration,
            ResourceLoader resourceLoader) {
        if (expiration <= 0) {
            throw new IllegalStateException("jwt.expiration must be greater than zero");
        }
        if (signingKeyId == null || signingKeyId.isBlank()) {
            throw new IllegalStateException("jwt.signing.key-id must not be blank");
        }

        this.expiration = expiration;
        this.signingKeyId = signingKeyId.trim();
        this.signingKey = loadPrivateKey(resourceLoader, privateKeyLocation);
        this.verificationKeys = loadVerificationKeys(resourceLoader, verificationKeyLocations);
        PublicKey activeVerificationKey = verificationKeys.get(this.signingKeyId);
        if (activeVerificationKey == null) {
            throw new IllegalStateException(
                    "jwt.verification-keys must include the active signing key ID: " + this.signingKeyId);
        }
        verifyKeyPair(signingKey, activeVerificationKey);

        Locator<Key> keyLocator = new LocatorAdapter<>() {
            @Override
            protected PublicKey locate(JwsHeader header) {
                if (!Jwts.SIG.RS256.getId().equals(header.getAlgorithm())) {
                    throw new UnsupportedJwtException("Only RS256 JWTs are accepted");
                }
                String keyId = header.getKeyId();
                PublicKey key = JwtUtil.this.verificationKeys.get(keyId);
                if (key == null) {
                    throw new UnsupportedJwtException("JWT contains an unknown key ID");
                }
                return key;
            }
        };
        this.parser = Jwts.parser()
                .keyLocator(keyLocator)
                .build();
    }

    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .header()
                .keyId(signingKeyId)
                .and()
                .subject(userDetails.getUserId().toString())
                .claim("username", userDetails.getUsername())
                .claim("roles", userDetails.getRoles())
                .claim("permissions", userDetails.getPermissions())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();
    }

    public TokenClaims parseToken(String token) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        String subject = claims.getSubject();
        String username = claims.get("username", String.class);

        if (subject == null || username == null || username.isBlank()) {
            throw new IllegalArgumentException("JWT must contain a subject and username");
        }

        try {
            Long userId = Long.valueOf(subject);
            return new TokenClaims(
                    userId,
                    username,
                    readStringListClaim(claims, "roles"),
                    readStringListClaim(claims, "permissions"));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("JWT subject must be a numeric user ID", exception);
        }
    }

    private List<String> readStringListClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (!(value instanceof List<?> values)
                || values.stream().anyMatch(item -> !(item instanceof String))) {
            throw new IllegalArgumentException("JWT claim must be a list of strings: " + claimName);
        }
        return values.stream()
                .map(String.class::cast)
                .toList();
    }

    private PrivateKey loadPrivateKey(ResourceLoader resourceLoader, String location) {
        try {
            byte[] encodedKey = readPem(resourceLoader, location, "PRIVATE KEY");
            PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
            validateRsaKeySize(((RSAPrivateKey) key).getModulus().bitLength(), location);
            return key;
        } catch (IOException | GeneralSecurityException | ClassCastException exception) {
            throw new IllegalStateException("Unable to load a valid RSA PKCS#8 private key from " + location, exception);
        }
    }

    private Map<String, PublicKey> loadVerificationKeys(ResourceLoader resourceLoader, String locations) {
        if (locations == null || locations.isBlank()) {
            throw new IllegalStateException("jwt.verification-keys must contain at least one key");
        }

        Map<String, PublicKey> keys = new HashMap<>();
        for (String entry : locations.split(",")) {
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator == entry.length() - 1) {
                throw new IllegalStateException(
                        "Each jwt.verification-keys entry must have the format key-id=resource-location");
            }
            String keyId = entry.substring(0, separator).trim();
            String location = entry.substring(separator + 1).trim();
            if (keyId.isEmpty() || location.isEmpty() || keys.containsKey(keyId)) {
                throw new IllegalStateException("JWT verification key IDs and locations must be valid and unique");
            }

            try {
                byte[] encodedKey = readPem(resourceLoader, location, "PUBLIC KEY");
                PublicKey key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedKey));
                validateRsaKeySize(((RSAPublicKey) key).getModulus().bitLength(), location);
                keys.put(keyId, key);
            } catch (IOException | GeneralSecurityException | ClassCastException exception) {
                throw new IllegalStateException("Unable to load a valid RSA X.509 public key from " + location, exception);
            }
        }
        return Map.copyOf(keys);
    }

    private byte[] readPem(ResourceLoader resourceLoader, String location, String keyType) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        try (var input = resource.getInputStream()) {
            String pem = new String(input.readAllBytes(), StandardCharsets.US_ASCII);
            String encodedKey = pem
                    .replace("-----BEGIN " + keyType + "-----", "")
                    .replace("-----END " + keyType + "-----", "")
                    .replaceAll("\\s", "");
            return Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Invalid PEM encoding for " + location, exception);
        }
    }

    private void validateRsaKeySize(int keySize, String location) {
        if (keySize < MIN_RSA_KEY_SIZE) {
            throw new IllegalStateException(
                    "RSA keys must be at least " + MIN_RSA_KEY_SIZE + " bits: " + location);
        }
    }

    private void verifyKeyPair(PrivateKey privateKey, PublicKey publicKey) {
        try {
            byte[] challenge = "jwt-key-pair-validation".getBytes(StandardCharsets.US_ASCII);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(challenge);
            byte[] signedChallenge = signature.sign();
            signature.initVerify(publicKey);
            signature.update(challenge);
            if (!signature.verify(signedChallenge)) {
                throw new IllegalStateException("The active JWT private and public keys do not match");
            }
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to validate the active JWT key pair", exception);
        }
    }

    public record TokenClaims(Long userId, String username, List<String> roles, List<String> permissions) {
    }
}
