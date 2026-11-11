package com.core.back.repository;

import com.core.back.entity.RoleEntity;
import com.core.back.payload.RoleRequest;

import java.util.List;
import java.util.Optional;

public interface IRoleRepository {
    RoleRequest saveRole(RoleRequest roleRequest);
    List<RoleRequest> findAllRoles();
    RoleRequest updateRole(Long id, RoleRequest roleRequest);
    void deleteRole(Long id);
    Optional<RoleEntity> findRoleById(Long roleId);
    Optional<RoleEntity> findRoleByName(String name);
    List<RoleEntity> findRolesByUserId(Long userId);
    void assignRoleToUser(Long userId, Long roleId);
    void removeRoleFromUser(Long userId, Long roleId);
}