package com.core.back.service;

import java.util.List;

import com.core.back.payload.RoleRequest;

public interface IRoleService {
    RoleRequest saveRole(RoleRequest roleRequest);
    List<RoleRequest> getAllRoles();
    RoleRequest updateRole(Long id, RoleRequest roleRequest);
    void deleteRole(Long id);
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);
}