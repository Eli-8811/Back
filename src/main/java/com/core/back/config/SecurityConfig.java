package com.core.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.core.back.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // CRUCIAL: Activa el soporte para evaluar @PreAuthorize con tus permisos atómicos
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, 
	                                       JwtAuthenticationFilter jwtFilter,
	                                       CorsConfigurationSource corsConfigurationSource) throws Exception {

	    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            // Las rutas aquí deben ser relativas al controlador, NO incluyas /api
	            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
	            .requestMatchers("/v0/auth/**").permitAll()
	            .requestMatchers("/v0/usuarios/**").permitAll()
	            .requestMatchers("/v0/role/**").permitAll()
	            .requestMatchers("/v0/permisos/**").permitAll()
	            .anyRequest().authenticated()
	        )
	        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

}