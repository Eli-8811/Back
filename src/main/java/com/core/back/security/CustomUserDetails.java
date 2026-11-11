package com.core.back.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class CustomUserDetails extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final List<String> roles;
    private final List<String> permissions;

    public Long getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public CustomUserDetails(
            Long userId,
            String username,
            String password,
            boolean enabled,
            Collection<String> roles,
            Collection<String> permissions) {
        this(userId, username, password, enabled, normalizeAuthorities(roles, permissions));
    }

    private CustomUserDetails(
            Long userId,
            String username,
            String password,
            boolean enabled,
            NormalizedAuthorities normalizedAuthorities) {
        super(username, password, enabled, true, true, true, normalizedAuthorities.authorities());
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.roles = normalizedAuthorities.roles();
        this.permissions = normalizedAuthorities.permissions();
    }

    private static NormalizedAuthorities normalizeAuthorities(
            Collection<String> roles,
            Collection<String> permissions) {
        Objects.requireNonNull(roles, "roles must not be null");
        Objects.requireNonNull(permissions, "permissions must not be null");
        List<String> normalizedRoles = normalizeRoles(roles);
        List<String> normalizedPermissions = normalizePermissions(permissions);
        List<GrantedAuthority> authorities = Stream.concat(
                        normalizedRoles.stream(),
                        normalizedPermissions.stream())
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new NormalizedAuthorities(normalizedRoles, normalizedPermissions, authorities);
    }

    private static List<String> normalizeRoles(Collection<String> roles) {
        return roles.stream()
                .map(role -> {
                    if (role == null || role.isBlank()) {
                        throw new IllegalArgumentException("Role names must not be blank");
                    }
                    String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
                    return normalizedRole.startsWith("ROLE_")
                            ? normalizedRole
                            : "ROLE_" + normalizedRole;
                })
                .distinct()
                .toList();
    }

    private static List<String> normalizePermissions(Collection<String> permissions) {
        return permissions.stream()
                .map(permission -> {
                    if (permission == null || permission.isBlank()) {
                        throw new IllegalArgumentException("Permission names must not be blank");
                    }
                    return permission.trim().toUpperCase(Locale.ROOT);
                })
                .distinct()
                .toList();
    }

    private record NormalizedAuthorities(
            List<String> roles,
            List<String> permissions,
            List<GrantedAuthority> authorities) {
    }
}