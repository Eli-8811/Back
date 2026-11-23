package com.core.back.controller;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.MfaCompletionResponse;
import com.core.back.payload.MfaEnrollmentConfirmationRequest;
import com.core.back.payload.MfaVerificationRequest;
import com.core.back.service.ILoginRateLimiterService;
import com.core.back.service.impl.MfaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0/mfa")
public class MfaController {

    private final MfaService mfaService;
    private final ILoginRateLimiterService loginRateLimiterService;

    public MfaController(MfaService mfaService, ILoginRateLimiterService loginRateLimiterService) {
        this.mfaService = mfaService;
        this.loginRateLimiterService = loginRateLimiterService;
    }

    @PostMapping("/verify")
    public ResponseEntity<GenericResponse<MfaCompletionResponse>> verify(
            @Valid @RequestBody MfaVerificationRequest request,
            HttpServletRequest httpRequest) {
        MfaCompletionResponse completion = mfaService.verify(
                request.challengeToken(),
                request.code(),
                request.recoveryCode());
        loginRateLimiterService.reset(httpRequest);
        return ResponseEntity.ok(GenericResponse.ok("Authentication completed successfully.", completion));
    }

    @PostMapping("/enroll/confirm")
    public ResponseEntity<GenericResponse<MfaCompletionResponse>> confirmEnrollment(
            @Valid @RequestBody MfaEnrollmentConfirmationRequest request,
            HttpServletRequest httpRequest) {
        MfaCompletionResponse completion = mfaService.confirmEnrollment(
                request.challengeToken(),
                request.code());
        loginRateLimiterService.reset(httpRequest);
        return ResponseEntity.ok(GenericResponse.ok("MFA enrollment completed successfully.", completion));
    }
}
