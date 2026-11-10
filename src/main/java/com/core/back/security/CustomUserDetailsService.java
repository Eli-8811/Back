package com.core.back.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.core.back.entity.PermissionEntity;
import com.core.back.entity.RoleEntity;
import com.core.back.entity.UserEntity;
import com.core.back.repository.IPermissionRepository;
import com.core.back.repository.IRoleRepository;
import com.core.back.repository.IUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository usuarioRepository;
    private final IRoleRepository rolRepository;
    private final IPermissionRepository permisoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info(">>>> [SECURITY] Intentando autenticar al usuario: '{}'", username);
        
        // 1. Recuperar el usuario maestro desde Oracle
        UserEntity usuario = usuarioRepository.buscarPorUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado en Oracle: " + username));

        log.info(">>>> [SECURITY] Registro maestro localizado para ID: {}", usuario.getUsuarioId());
        
        // 2. Recuperar sus roles asociados desde su repositorio especializado
        List<RoleEntity> rolesEntidad = rolRepository.buscarRolesPorUsuarioId(usuario.getUsuarioId());
        
        List<RolAuthority> rolesSecurity = new ArrayList<>();
        List<GrantedAuthority> todasLasAutoridades = new ArrayList<>();

        // 3. Poblar la jerarquía de permisos de forma explícita (Mapeo manual JDBC)
        for (RoleEntity rol : rolesEntidad) {
            List<PermissionEntity> permisosEntidad = permisoRepository.buscarPermisosPorRolId(rol.getRolId());
            
            // Adjuntamos la lista al POJO para mantener la integridad del modelo
            rol.setPermisos(permisosEntidad);

            // Convertimos los permisos atómicos a SimpleGrantedAuthority
            List<GrantedAuthority> permisosSecurity = permisosEntidad.stream()
                    .map(p -> new SimpleGrantedAuthority(p.getNombre().toUpperCase().trim()))
                    .collect(Collectors.toList());

            // Creamos nuestra estructura robusta de Rol jerárquico
            RolAuthority rolAuthority = new RolAuthority(rol.getNombre(), permisosSecurity);
            rolesSecurity.add(rolAuthority);

            // Alimentamos el saco global de Spring Security con el Rol y sus Permisos atómicos
            todasLasAutoridades.add(rolAuthority);         // Ejemplo: "ROLE_ADMIN"
            todasLasAutoridades.addAll(permisosSecurity);  // Ejemplo: "PERMISO_DESCARGAR_REPORTE"
        }

        // Seteamos el árbol de seguridad completo dentro de la entidad de negocio
        usuario.setRoles(rolesEntidad);

        log.debug(">>>> [SECURITY] Carga completa. Autoridades totales asignadas: {}", todasLasAutoridades);

        // 4. Retornamos nuestro CustomUserDetails de alto rendimiento
        return new CustomUserDetails(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getEstado() != null && usuario.getEstado(),
                rolesSecurity,
                todasLasAutoridades
        );
    }
    
}