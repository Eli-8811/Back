package com.core.back.repository;

import com.core.back.entity.PermissionEntity;
import java.util.List;
import java.util.Optional;

public interface IPermissionRepository {
    Optional<PermissionEntity> findPermissionById(Long permissionId);
    List<PermissionEntity> findPermissionsByRoleId(Long roleId);
    void assignPermissionToRole(Long roleId, Long permissionId);
    void removePermissionFromRole(Long roleId, Long permissionId);
    void removeAllPermissionsFromRole(Long roleId);
    PermissionEntity savePermission(PermissionEntity permission);
    void updatePermission(PermissionEntity permission);
    void deletePermission(Long permissionId);
    List<PermissionEntity> findAllPermissions();
}