package com.core.back.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Extensión personalizada del objeto User nativo de Spring Security
 * para almacenar la estructura jerárquica de roles y permisos.
 */
public class CustomUserDetails extends User {

    private static final long serialVersionUID = 1L;
    
    // Guardamos la colección estructurada de roles con sus permisos dentro
    private final Collection<RolAuthority> roles;

    public Collection<RolAuthority> getRoles() {
        return roles;
    }

    public CustomUserDetails(String username, String password, boolean enabled, 
                             Collection<RolAuthority> roles, 
                             Collection<? extends GrantedAuthority> todasLasAutoridades) {
        
        // Enviamos las credenciales y la lista plana de autoridades a la superclase
        // para mantener la compatibilidad con @PreAuthorize de forma nativa
        super(username, password, enabled, true, true, true, todasLasAutoridades);
        this.roles = roles;
    }
    
}