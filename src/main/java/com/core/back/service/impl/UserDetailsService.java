package com.core.back.service.impl;

import com.core.back.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.core.back.entity.RoleEntity;
import com.core.back.entity.PermissionEntity;
import com.core.back.entity.UserEntity;
import com.core.back.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsService.class);
    private final IUserRepository userRepository;

    public UserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String trimmedUsername = username == null ? "" : username.trim();

        if (trimmedUsername.isEmpty()) {
            throw new UsernameNotFoundException("Username must not be blank");
        }

        UserEntity user = userRepository.findUserDataByUsername(trimmedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + trimmedUsername));
        log.debug("Loaded user details for authentication");
        List<RoleEntity> userRoles = user.getRoles() != null ? user.getRoles() : List.of();
        LinkedHashSet<String> roles = new LinkedHashSet<>();
        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        for (RoleEntity role : userRoles) {
            roles.add(role.getName());
            if (role.getPermissions() != null) {
                role.getPermissions().stream()
                        .map(PermissionEntity::getName)
                        .forEach(permissions::add);
            }
        }

        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getActive() != null && user.getActive(),
                roles,
                permissions
        );
    }

}