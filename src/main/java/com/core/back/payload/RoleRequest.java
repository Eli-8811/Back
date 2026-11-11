package com.core.back.payload;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoleRequest {

    private Long roleId;

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Perspective must not exceed 255 characters")
    private String perspective;

    private Boolean active;

    @Valid
    private List<PermissionRequest> permissions;

    public RoleRequest() {
    }

    public RoleRequest(Long roleId, String name, String perspective, Boolean active) {
        this(roleId, name, perspective, active, null);
    }

    public RoleRequest(Long roleId, String name, String perspective, Boolean active,
                       List<PermissionRequest> permissions) {
        this.roleId = roleId;
        this.name = name;
        this.perspective = perspective;
        this.active = active;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<PermissionRequest> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionRequest> permissions) {
        this.permissions = permissions;
    }
    
}