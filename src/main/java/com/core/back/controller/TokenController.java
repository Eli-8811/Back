package com.core.back.controller;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.JwtResponse;
import com.core.back.payload.MfaCompletionResponse;
import com.core.back.payload.RefreshTokenRequest;
import com.core.back.service.ILoginRateLimiterService;
import com.core.back.service.impl.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v0/token")
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final ILoginRateLimiterService loginRateLimiterService;

    public TokenController(
            RefreshTokenService refreshTokenService,
            ILoginRateLimiterService loginRateLimiterService) {
        this.refreshTokenService = refreshTokenService;
        this.loginRateLimiterService = loginRateLimiterService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<GenericResponse<MfaCompletionResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        RefreshTokenService.RotatedTokens tokens = refreshTokenService.rotate(request.refreshToken());
        loginRateLimiterService.reset(httpRequest);
        MfaCompletionResponse response = new MfaCompletionResponse(
                new JwtResponse("Bearer", tokens.accessToken()),
                List.of(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiresInSeconds());
        return ResponseEntity.ok(GenericResponse.ok("Token refreshed successfully.", response));
    }
}
