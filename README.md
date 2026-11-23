# Back
Spring boot

## Mandatory MFA sign-in

Sign-in requires a TOTP authenticator app. After the password is accepted, `POST /api/v0/signin` returns HTTP `202` with a short-lived `challengeToken` and a `nextStep`:

- `ENROLL_TOTP`: first-time setup. The response includes an `otpauth://` `provisioningUri` and a `manualEntryKey` for the authenticator app. The frontend can render a QR code from the URI.
- `VERIFY_TOTP`: enter the current six-digit authenticator code.

Complete first-time setup with `POST /api/v0/mfa/enroll/confirm`, sending `challengeToken` and `code`. Complete a later sign-in with `POST /api/v0/mfa/verify`, sending `challengeToken` and either `code` or `recoveryCode`. A successful response contains the normal Bearer JWT. Initial enrollment also returns ten one-use recovery codes; show and store them securely because they are not returned again. A challenge expires after five minutes and cannot be reused.

The authenticator uses RFC 6238 TOTP with a 30-second period and six digits. Each accepted TOTP time step can be used only once per account. Login, MFA completion, and token refresh attempts share the existing limit of five requests per minute per client IP.

After MFA succeeds, the response also includes `refreshToken` and `refreshTokenExpiresInSeconds` (30 days by default). The 30-day lifetime is fixed from MFA completion and is not extended by rotation. Keep the refresh token in secure client-side storage. When the short-lived access token expires, call `POST /api/v0/token/refresh` with `{"refreshToken":"..."}`. The response has a new access token and a rotated refresh token; replace the old refresh token immediately because it cannot be reused. Refresh tokens are stored as hashes in Redis. Configure the lifetime with `JWT_REFRESH_EXPIRATION_SECONDS`.

To close the session, call `POST /api/v0/signout` with the access token in the `Authorization: Bearer ...` header and `{"refreshToken":"..."}` in the JSON body. This revokes both tokens. Omitting the body preserves compatibility with access-token-only clients but does not revoke the refresh token.

### Deployment

Apply `src/main/resources/db/mfa-schema.sql` to PostgreSQL before deploying. In production, configure `MFA_ENCRYPTION_KEY` as a Base64-encoded, randomly generated 32-byte key (for example, generate it with `openssl rand -base64 32`) and optionally set `MFA_ISSUER`. Keep the encryption key in a secret manager, back it up securely, and do not change it without re-encrypting stored MFA secrets. The key is required outside the `dev` profile.

For local development, activate the `dev` Spring profile (`--spring.profiles.active=dev` or set `SPRING_PROFILES_ACTIVE=dev`). That profile supplies a fixed fallback only when `MFA_ENCRYPTION_KEY` is absent; the environment variable overrides it. The fallback is public and must never be used with real accounts or in production. Redis must be available for short-lived MFA challenges. Serve sign-in and enrollment only over HTTPS because the enrollment response contains the TOTP secret.

Existing accounts without an MFA record are required to enroll the next time their password is accepted. No normal JWT is issued before TOTP confirmation.
