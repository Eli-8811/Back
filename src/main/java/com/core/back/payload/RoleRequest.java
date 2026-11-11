package com.core.back.payload;

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

    public RoleRequest() {
    }

    public RoleRequest(Long roleId, String name, String perspective, Boolean active) {
        this.roleId = roleId;
        this.name = name;
        this.perspective = perspective;
        this.active = active;
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
    
}