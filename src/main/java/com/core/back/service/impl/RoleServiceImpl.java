package com.core.back.service.impl;

import com.core.back.exception.ResourceNotFoundException;
import com.core.back.payload.RoleRequest;
import com.core.back.repository.IPermissionRepository;
import com.core.back.repository.IRoleRepository;
import com.core.back.service.IRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements IRoleService {

    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;

    public RoleServiceImpl(
            IRoleRepository roleRepository,
            IPermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public RoleRequest saveRole(RoleRequest roleRequest) {
        return roleRepository.saveRole(roleRequest);
    }

    @Override
    public List<RoleRequest> getAllRoles() {
        return roleRepository.findAllRoles();
    }

    @Override
    public RoleRequest updateRole(Long id, RoleRequest roleRequest) {
        roleRepository.findRoleById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        return roleRepository.updateRole(id, roleRequest);
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.findRoleById(id).orElseThrow(() -> new ResourceNotFoundException("Cannot delete role: no role exists with ID " + id));
        roleRepository.deleteRole(id);
        permissionRepository.removeAllPermissionsFromRole(id);
    }
    
    @Override
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        roleRepository.findRoleById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        permissionRepository.removeAllPermissionsFromRole(roleId);
        for (Long permissionId : permissionIds) {
            permissionRepository.assignPermissionToRole(roleId, permissionId);
        }
    }
    
}