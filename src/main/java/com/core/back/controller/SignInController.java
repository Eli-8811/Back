package com.core.back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.JwtResponse;
import com.core.back.payload.SignInRequest;
import com.core.back.security.CustomUserDetails;
import com.core.back.service.ILoginRateLimiterService;
import com.core.back.utils.JwtUtil;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v0/signin")
public class SignInController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ILoginRateLimiterService loginRateLimiterService;

    public SignInController(
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            ILoginRateLimiterService loginRateLimiterService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.loginRateLimiterService = loginRateLimiterService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<Object>> signIn(@Valid @RequestBody SignInRequest signInRequest, HttpServletRequest httpRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername().trim(), signInRequest.getPassword())
        );

        loginRateLimiterService.reset(httpRequest);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtUtil.generateToken(userDetails);

        JwtResponse jwtResponse = new JwtResponse("Bearer", token);

        return ResponseEntity.ok(new GenericResponse<>(true, "Authentication completed successfully.", jwtResponse));

    }

}
