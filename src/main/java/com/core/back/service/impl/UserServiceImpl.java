package com.core.back.service.impl;

import com.core.back.entity.RoleEntity;
import com.core.back.entity.UserEntity;
import com.core.back.exception.ConflictException;
import com.core.back.exception.ResourceNotFoundException;
import com.core.back.exception.ValidationException;
import com.core.back.payload.UserRequest;
import com.core.back.repository.IUserRepository;
import com.core.back.service.IUserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.security.password-regex}")
    private String passwordRegex;

    public UserServiceImpl(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    private UserRequest toDto(UserEntity entidad) {
        return new UserRequest(
                entidad.getUserId(),
                entidad.getFirstName(),
                entidad.getLastName(),
                entidad.getAge(),
                entidad.getGender(),
                entidad.getUsername(),
                null,
                entidad.getEmail(),
                entidad.getActive(),
                null);
    }

    private UserEntity toEntity(UserRequest dto) {
        UserEntity user = new UserEntity();
        user.setUserId(dto.getUserId());
        user.setFirstName(dto.getFirstName() != null ? dto.getFirstName().trim() : null);
        user.setLastName(dto.getLastName() != null ? dto.getLastName().trim() : null);
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        user.setGender(dto.getGender());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRoles(dto.getRoleIds() != null
                ? dto.getRoleIds().stream()
                        .map(id -> {
                            RoleEntity role = new RoleEntity();
                            role.setRoleId(id);
                            return role;
                        })
                        .collect(Collectors.toList())
                : null);
        return user;
    }
    
    @Override
    @Transactional
    public UserRequest createUser(UserRequest userRequest) {
        // 1. Validar reglas de negocio
        validarDatosUsuario(userRequest);
        
        // 2. Persistir usuario
        UserEntity entidad = toEntity(userRequest);
        entidad.setPassword(passwordEncoder.encode(userRequest.getPassword())); 
        UserEntity guardado = userRepository.saveUser(entidad);        
        
        // 3. Persistir relaciones de roles
        asignarRolesSiExisten(guardado.getUserId(), userRequest.getRoleIds());
        
        return toDto(guardado);
    }

    private void validarDatosUsuario(UserRequest dto) {
        if (userRepository.existsUserByEmail(dto.getEmail())) {
            throw new ConflictException("Email '" + dto.getEmail() + "' is already registered.");
        }
        if (userRepository.existsUserByUsername(dto.getUsername())) {
            throw new ConflictException("Username '" + dto.getUsername() + "' is already in use.");
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
                if (userRepository.existsRoleById(rolId)) {
                    userRepository.assignRoleToUser(usuarioId, rolId);
                } else {
                    throw new ValidationException("Role with ID " + rolId + " does not exist.");
                }
            }
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> getActiveUsers() {
        // Obtenemos el Optional y extraemos la lista, o devolvemos una lista vacía si es null/empty
        return userRepository.findActiveUsers(true).orElse(Collections.emptyList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserRequest getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));
    }

    @Override
    @Transactional
    public UserRequest updateUser(Long userId, UserRequest userRequest) {
        UserEntity usuario = userRepository.findUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("The user to update was not found."));
        
        if (!usuario.getEmail().equalsIgnoreCase(userRequest.getEmail()) && userRepository.existsUserByEmail(userRequest.getEmail())) {
            throw new ConflictException("Email '" + userRequest.getEmail() + "' is already in use by another user.");
        }
        
        usuario.setFirstName(userRequest.getFirstName() != null ? userRequest.getFirstName().trim() : usuario.getFirstName());
        usuario.setLastName(userRequest.getLastName() != null ? userRequest.getLastName().trim() : usuario.getLastName());
        usuario.setAge(userRequest.getAge());
        usuario.setGender(userRequest.getGender());
        usuario.setEmail(userRequest.getEmail());        
        
        return toDto(userRepository.updateUser(usuario));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new ResourceNotFoundException("Cannot delete user: no user exists with ID " + userId + ".");
        }
        userRepository.deleteUserById(userId);
    }
    
}