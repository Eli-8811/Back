package com.core.back.controller;

import com.core.back.payload.GenericResponse;
import com.core.back.service.impl.TokenBlacklistService;
import com.core.back.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0/auth")
public class LogoutController {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public LogoutController(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = resolveBearerToken(authorizationHeader);
        JwtUtil.TokenClaims claims = jwtUtil.parseToken(token);
        long remainingExpirationMillis = jwtUtil.getExpirationTimeRemaining(token);
        tokenBlacklistService.blacklistToken(claims.tokenId(), remainingExpirationMillis);

        return ResponseEntity.ok(GenericResponse.ok("Sesión cerrada correctamente.", null));
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Se requiere un token Bearer válido.");
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Se requiere un token Bearer válido.");
        }
        return token;
    }
}
