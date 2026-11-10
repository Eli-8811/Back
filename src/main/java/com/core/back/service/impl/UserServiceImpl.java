package com.core.back.service.impl;

import com.core.back.entity.RoleEntity;
import com.core.back.entity.UserEntity;
import com.core.back.exception.ConflictException;
import com.core.back.exception.ResourceNotFoundException;
import com.core.back.exception.ValidationException;
import com.core.back.payload.UserRequest;
import com.core.back.repository.IUserRepository;
import com.core.back.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // ASPECTO: Patrón de Diseño (Inyección de dependencias por constructor)
public class UserServiceImpl implements IUserService {

    private final IUserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.security.password-regex}")
    private String passwordRegex;
    
    private UserRequest toDto(UserEntity entidad) {
        return UserRequest.builder()
                .usuarioId(entidad.getUsuarioId())
                .nombre(entidad.getNombre())
                .apellidos(entidad.getApellidos())
                .correo(entidad.getCorreo())
                .edad(entidad.getEdad())
                .genero(entidad.getGenero())
                .username(entidad.getUsername())
                .estado(entidad.getEstado())
                .build();
    }

    private UserEntity toEntity(UserRequest dto) {
        return UserEntity.builder()
                .usuarioId(dto.getUsuarioId())
                .nombre(dto.getNombre() != null ? dto.getNombre().trim() : null)
                .apellidos(dto.getApellidos() != null ? dto.getApellidos().trim() : null)
                .correo(dto.getCorreo())
                .edad(dto.getEdad())
                .genero(dto.getGenero())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .roles(dto.getRolesIds() != null ? 
                       dto.getRolesIds().stream()
                          .map(id -> {
                              RoleEntity rol = new RoleEntity();
                              rol.setRolId(id);
                              return rol;
                          })
                          .collect(Collectors.toList()) 
                       : null)
                .build();
    }
    
    @Override
    @Transactional
    public UserRequest guardarUsuario(UserRequest usuarioDto) {
        // 1. Validar reglas de negocio
        validarDatosUsuario(usuarioDto);
        
        // 2. Persistir usuario
        UserEntity entidad = toEntity(usuarioDto);
        entidad.setPassword(passwordEncoder.encode(usuarioDto.getPassword())); 
        UserEntity guardado = usuarioRepository.guardar(entidad);        
        
        // 3. Persistir relaciones de roles
        asignarRolesSiExisten(guardado.getUsuarioId(), usuarioDto.getRolesIds());
        
        return toDto(guardado);
    }

    private void validarDatosUsuario(UserRequest dto) {
        if (usuarioRepository.existePorCorreo(dto.getCorreo())) {
            throw new ConflictException("El correo '" + dto.getCorreo() + "' ya está registrado.");
        }
        if (usuarioRepository.existePorUsername(dto.getUsername())) {
            throw new ConflictException("El usuario '" + dto.getUsername() + "' ya está en uso.");
        }
        
        if (dto.getPassword() == null || !dto.getPassword().matches(passwordRegex)) {
            // Usamos tu ValidationException personalizada para un 400 Bad Request
            throw new ValidationException(); 
        }
    }

    private void asignarRolesSiExisten(Long usuarioId, List<Long> rolesIds) {
        if (rolesIds != null && !rolesIds.isEmpty()) {
            for (Long rolId : rolesIds) {
                // Validación para evitar el ORA-02291
                if (usuarioRepository.existeRolPorId(rolId)) {
                    usuarioRepository.asignarRol(usuarioId, rolId);
                } else {
                    throw new ValidationException("El rol con ID " + rolId + " no existe.");
                }
            }
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> obtenerUsuariosActivos() {
        log.info("Consultando lista de usuarios activos...");        
        // Obtenemos el Optional y extraemos la lista, o devolvemos una lista vacía si es null/empty
        return usuarioRepository.obtenerUsuariosActivos(true).orElse(Collections.emptyList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserRequest buscarUsuarioPorCorreo(String correo) {
        log.info("Buscando usuario por correo: {}", correo);
        return usuarioRepository.buscarPorCorreo(correo)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("No existe usuario con el correo: " + correo));
    }

    @Override
    @Transactional
    public UserRequest actualizarUsuario(Long usuarioId, UserRequest dto) {
        log.info("Actualizando datos del usuario ID: {}", usuarioId);        
        
        UserEntity usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario a actualizar no existe."));
        
        if (!usuario.getCorreo().equalsIgnoreCase(dto.getCorreo()) && usuarioRepository.existePorCorreo(dto.getCorreo())) {
            throw new ConflictException("El nuevo correo '" + dto.getCorreo() + "' ya está en uso por otro perfil.");
        }
        
        usuario.setNombre(dto.getNombre() != null ? dto.getNombre().trim() : usuario.getNombre());
        usuario.setApellidos(dto.getApellidos() != null ? dto.getApellidos().trim() : usuario.getApellidos());
        usuario.setEdad(dto.getEdad());
        usuario.setGenero(dto.getGenero());
        usuario.setCorreo(dto.getCorreo());        
        
        return toDto(usuarioRepository.actualizar(usuario));
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long usuarioId) {
        log.info("Solicitud de eliminación para ID: {}", usuarioId);        
        if (!usuarioRepository.existePorId(usuarioId)) {
            throw new ResourceNotFoundException("No se puede eliminar: El usuario con ID " + usuarioId + " no existe.");
        }
        usuarioRepository.eliminarPorId(usuarioId);
    }
    
}