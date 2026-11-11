package com.core.back.security;

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

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;

    public CustomUserDetailsService(
            IUserRepository userRepository,
            IRoleRepository roleRepository,
            IPermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findUserByUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<RoleEntity> roles = roleRepository.findRolesByUserId(user.getUserId());
        
        List<RolAuthority> roleAuthorities = new ArrayList<>();
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (RoleEntity role : roles) {
            List<PermissionEntity> permissions = permissionRepository.findPermissionsByRoleId(role.getRoleId());
            
            role.setPermissions(permissions);

            List<GrantedAuthority> permissionAuthorities = permissions.stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName().toUpperCase().trim()))
                    .collect(Collectors.toList());

            RolAuthority roleAuthority = new RolAuthority(role.getName(), permissionAuthorities);
            roleAuthorities.add(roleAuthority);

            authorities.add(roleAuthority);
            authorities.addAll(permissionAuthorities);
        }

        user.setRoles(roles);

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                user.getActive() != null && user.getActive(),
                roleAuthorities,
                authorities
        );
    }
    
}