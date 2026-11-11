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
@RequestMapping("/v0/auth/signin")
public class SignInController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ILoginRateLimiterService loginRateLimiterService;

    public SignInController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            ILoginRateLimiterService loginRateLimiterService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.loginRateLimiterService = loginRateLimiterService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<Object>> signIn(
            @Valid @RequestBody SignInRequest request,
            HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
        );
        loginRateLimiterService.reset(httpRequest);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        JwtResponse jwtResponse = new JwtResponse("Bearer", token);
        return ResponseEntity.ok(new GenericResponse<>(true, "Authentication completed successfully.", jwtResponse));
    }
}
