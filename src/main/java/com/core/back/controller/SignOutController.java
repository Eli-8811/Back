package com.core.back.controller;

import com.core.back.payload.GenericResponse;
import com.core.back.service.ITokenBlacklistService;
import com.core.back.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0/signout")
public class SignOutController {

    private final JwtUtil jwtUtil;
    private final ITokenBlacklistService tokenBlacklistService;

    public SignOutController(JwtUtil jwtUtil, ITokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = resolveBearerToken(authorizationHeader);
        JwtUtil.TokenClaims claims = jwtUtil.parseToken(token);
        long remainingExpirationMillis = jwtUtil.getExpirationTimeRemaining(token);
        tokenBlacklistService.blacklistToken(claims.tokenId(), remainingExpirationMillis);
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
