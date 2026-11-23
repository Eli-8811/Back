package com.core.back.controller;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.RefreshTokenRequest;
import com.core.back.service.ITokenBlacklistService;
import com.core.back.service.impl.RefreshTokenService;
import com.core.back.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0/signout")
public class SignOutController {

    private final JwtUtil jwtUtil;
    private final ITokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    public SignOutController(
            JwtUtil jwtUtil,
            ITokenBlacklistService tokenBlacklistService,
            RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody(required = false) RefreshTokenRequest request) {
        String token = resolveBearerToken(authorizationHeader);
        JwtUtil.TokenClaims claims = jwtUtil.parseToken(token);
        long remainingExpirationMillis = jwtUtil.getExpirationTimeRemaining(token);
        tokenBlacklistService.blacklistToken(claims.tokenId(), remainingExpirationMillis);
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            refreshTokenService.revoke(request.refreshToken());
        }
        return ResponseEntity.ok(GenericResponse.ok("Session closed successfully.", null));
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("A valid Bearer token is required.");
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("A valid Bearer token is required.");
        }
        return token;
    }

}
