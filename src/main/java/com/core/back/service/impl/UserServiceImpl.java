package com.core.back.service.impl;

import com.core.back.entity.RoleEntity;
import com.core.back.entity.UserEntity;
import com.core.back.exception.ConflictException;
import com.core.back.exception.ResourceNotFoundException;
import com.core.back.exception.ValidationException;
import com.core.back.payload.PermissionRequest;
import com.core.back.payload.RoleRequest;
import com.core.back.payload.UserRequest;
import com.core.back.repository.IUserRepository;
import com.core.back.service.IUserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    
    private UserRequest toDto(UserEntity entity) {
        return new UserRequest(
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAge(),
                entity.getGender(),
                entity.getUsername(),
                null,
                entity.getEmail(),
                entity.getActive(),
                entity.getRoles() != null
                        ? entity.getRoles().stream()
                                .map(role -> new RoleRequest(
                                        role.getRoleId(),
                                        role.getName(),
                                        role.getPerspective(),
                                        role.getActive(),
                                        role.getPermissions() != null
                                                ? role.getPermissions().stream()
                                                        .map(permission -> new PermissionRequest(
                                                                permission.getPermissionId(),
                                                                permission.getName(),
                                                                permission.getPerspective(),
                                                                permission.getActive()))
                                                        .collect(Collectors.toList())
                                                : null))
                                .collect(Collectors.toList())
                        : null);
    }

    private UserEntity toEntity(UserRequest request) {
        UserEntity user = new UserEntity();
        user.setUserId(request.getUserId());
        user.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        user.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setActive(request.getActive());
        user.setRoles(request.getRoles() != null
                ? request.getRoles().stream()
                        .map(roleRequest -> {
                            RoleEntity role = new RoleEntity();
                            role.setRoleId(roleRequest.getRoleId());
                            role.setName(roleRequest.getName());
                            role.setPerspective(roleRequest.getPerspective());
                            role.setActive(roleRequest.getActive());
                            return role;
                        })
                        .collect(Collectors.toList())
                : null);
        return user;
    }
    
    @Override
    public UserRequest createUser(UserRequest userRequest) {
        validateUserData(userRequest);
        UserEntity entity = toEntity(userRequest);
        entity.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        UserEntity savedUser = userRepository.saveUser(entity);
        List<Long> roles = userRequest.getRoles() != null
                ? userRequest.getRoles().stream()
                        .map(RoleRequest::getRoleId)
                        .collect(Collectors.toList())
                : null;
        assignRolesIfExist(savedUser.getUserId(), roles);
        return toDto(savedUser);
    }

    private void assignRolesIfExist(Long userId, List<Long> roleIds) {
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                if (userRepository.existsRoleById(roleId)) {
                    userRepository.assignRoleToUser(userId, roleId);
                } else {
                    throw new ValidationException("Role with ID " + roleId + " does not exist.");
                }
            }
        }
    }

    private void validateUserData(UserRequest request) {
        if (userRepository.existsUserByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' is already registered.");
        }
        if (userRepository.existsUserByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' is already in use.");
        }
        
        if (request.getPassword() == null || !request.getPassword().matches(passwordRegex)) {
            // Usamos tu ValidationException personalizada para un 400 Bad Request
            throw new ValidationException("Invalid password format.");
        }
    }
    
    @Override
    public List<UserEntity> getActiveUsers() {
        return userRepository.findActiveUsers(true).orElse(Collections.emptyList());
    }
    
    @Override
    public UserRequest getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));
    }

    @Override
    public UserRequest updateUser(Long userId, UserRequest userRequest) {

        UserEntity user = userRepository.findUserById(userId).orElseThrow(() -> new ResourceNotFoundException("The user to update was not found."));
        
        if (!user.getEmail().equalsIgnoreCase(userRequest.getEmail()) && userRepository.existsUserByEmail(userRequest.getEmail())) {
            throw new ConflictException("Email '" + userRequest.getEmail() + "' is already in use by another user.");
        }
        
        user.setFirstName(userRequest.getFirstName() != null ? userRequest.getFirstName().trim() : user.getFirstName());
        user.setLastName(userRequest.getLastName() != null ? userRequest.getLastName().trim() : user.getLastName());
        user.setAge(userRequest.getAge());
        user.setGender(userRequest.getGender());
        user.setEmail(userRequest.getEmail());
        
        return toDto(userRepository.updateUser(user));
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new ResourceNotFoundException("Cannot delete user: no user exists with ID " + userId + ".");
        }
        userRepository.deleteUserById(userId);
    }
    
}