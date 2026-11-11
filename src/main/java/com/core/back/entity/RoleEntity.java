package com.core.back.entity;

import java.util.List;

public class RoleEntity extends BaseEntity {

    private Long roleId;
    private String name;
    private String perspective;
    private List<PermissionEntity> permissions;

    public RoleEntity() {
    }

    public RoleEntity(Long roleId, String name, String perspective, List<PermissionEntity> permissions) {
        this.roleId = roleId;
        this.name = name;
        this.perspective = perspective;
        this.permissions = permissions;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPerspective() {
        return perspective;
    }

    public void setPerspective(String perspective) {
        this.perspective = perspective;
    }

    public List<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionEntity> permissions) {
        this.permissions = permissions;
    }

}