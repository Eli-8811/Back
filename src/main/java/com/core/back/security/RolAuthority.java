package com.core.back.security;

import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

/**
 * Representa la autoridad de un Rol dentro del contexto de Spring Security,
 * manteniendo de forma estructurada su colección interna de permisos atómicos.
 */
public class RolAuthority implements GrantedAuthority {

    private static final long serialVersionUID = 1L;

    private final String nombreRol;
    private final Collection<? extends GrantedAuthority> permisos;

    public RolAuthority(String nombreRol, Collection<? extends GrantedAuthority> permisos) {
        if (nombreRol == null || nombreRol.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol no puede ser nulo o vacío");
        }
        
        String nombreLimpio = nombreRol.toUpperCase().trim();
        // Forzamos el estándar de Spring Security: si no empieza con ROLE_, se lo concatenamos
        this.nombreRol = nombreLimpio.startsWith("ROLE_") ? nombreLimpio : "ROLE_" + nombreLimpio;
        this.permisos = permisos;
    }

    /**
     * Este método es el que lee Spring Security internamente.
     * Al retornar "ROLE_ADMIN", permite evaluar anotaciones como @PreAuthorize("hasRole('ADMIN')")
     */
    @Override
    public String getAuthority() {
        return this.nombreRol;
    }

    /**
     * Retorna la colección de permisos atómicos asociados de forma exclusiva a este rol.
     * Crucial para estructurar de forma limpia el JWT hacia Angular Material.
     */
    public Collection<? extends GrantedAuthority> getPermisos() {
        return this.permisos;
    }

    @Override
    public String toString() {
        return "RolAuthority{" +
                "nombreRol='" + nombreRol + '\'' +
                ", cantidadPermisos=" + (permisos != null ? permisos.size() : 0) +
                '}';
    }
    
}