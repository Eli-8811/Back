package com.core.back.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.core.back.utils.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. IGNORAR RUTAS PÚBLICAS
        // Si es una ruta que permitiste en SecurityConfig, no intentes validar un JWT
        if (path.contains("/v1/auth/") || path.contains("/v1/usuarios")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            // 3. Extracción del Token Bearer
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtUtil.extractUsername(token);
            }

            // 4. Validación y seteo en el Contexto de Seguridad
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Esto invoca tu query JDBC optimizado con el árbol de roles y permisos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token)) {
                    // Pasamos las autoridades completas (Roles con prefijo + Permisos planos)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Inyectamos el token en el hilo de la petición
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug(">>>> [SECURITY] JWT validado exitosamente para el usuario: '{}'", username);
                }
            }
        } catch (Exception e) {
            // El catch evita que un token expirado o corrupto rompa la app, simplemente deniega el acceso con un 403 posterior
            log.error(">>>> [SECURITY] Falló la validación del JWT en la ruta {}: {}", path, e.getMessage());
        }

        // Continuar de forma normal con el flujo del filtro
        filterChain.doFilter(request, response);
    }
}