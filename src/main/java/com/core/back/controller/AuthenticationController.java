package com.core.back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.core.back.payload.GenericResponse; // Tu clase de payload estándar
import com.core.back.payload.JwtResponse;
import com.core.back.payload.LoginRequest;
import com.core.back.security.CustomUserDetails;
import com.core.back.utils.JwtUtil;

// Supongamos que tienes estos DTOs en tu paquete de payloads/request
// Puedes ajustar las importaciones según tus nombres exactos
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<GenericResponse<Object>> login(@Valid @RequestBody LoginRequest request) {
    	
        log.info(">>>> [AUTH] Intento de inicio de sesión para el usuario: '{}'", request.getUsername());

        // 1. Ejecuta la autenticación contra el AuthenticationManager (Invocará internamente a tu CustomUserDetailsService)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
        );

        // 2. Recuperamos nuestro UserDetails robusto con el árbol jerárquico de base de datos
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info(">>>> [AUTH] Autenticación Oracle exitosa para ID: '{}'", userDetails.getUsername());

        // 3. Generamos el token JWT (Tu JwtUtil recibirá este objeto con sus authorities listas)
        String token = jwtUtil.generateToken(userDetails);

        // 4. Estructuramos el DTO de salida para que Angular Material lo reciba
        JwtResponse jwtResponse = new JwtResponse("Bearer", token);

        // 5. Retornamos la respuesta estandarizada mediante tu formato global exitoso
        return ResponseEntity.ok(
                new GenericResponse<>(true, "Autenticación completada con éxito.", jwtResponse)
        );
        
    }
    
}