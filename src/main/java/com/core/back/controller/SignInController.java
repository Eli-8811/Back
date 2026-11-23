package com.core.back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.MfaChallengeResponse;
import com.core.back.payload.SignInRequest;
import com.core.back.security.CustomUserDetails;
import com.core.back.service.impl.MfaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v0/signin")
public class SignInController {

    private final AuthenticationManager authenticationManager;
    private final MfaService mfaService;

    public SignInController(
            AuthenticationManager authenticationManager,
            MfaService mfaService) {
        this.authenticationManager = authenticationManager;
        this.mfaService = mfaService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<MfaChallengeResponse>> signIn(
            @Valid @RequestBody SignInRequest signInRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername().trim(), signInRequest.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        MfaChallengeResponse challenge = mfaService.begin(userDetails);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new GenericResponse<>(
                        true,
                        "Password accepted. Complete the required MFA step to finish signing in.",
                        challenge));
    }

}
