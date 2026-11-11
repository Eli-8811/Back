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
import com.core.back.utils.JwtUtil;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signin")
    public ResponseEntity<GenericResponse<Object>> signIn(@Valid @RequestBody SignInRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        JwtResponse jwtResponse = new JwtResponse("Bearer", token);
        return ResponseEntity.ok(new GenericResponse<>(true, "Authentication completed successfully.", jwtResponse));
    }
}