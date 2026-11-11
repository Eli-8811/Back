package com.core.back.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PermissionRequest {

    private Long permissionId;

    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Perspective must not exceed 255 characters")
    private String perspective;

    private Boolean active;

    public PermissionRequest() {
    }

    public PermissionRequest(Long permissionId, String name, String perspective, Boolean active) {
        this.permissionId = permissionId;
        this.name = name;
        this.perspective = perspective;
        this.active = active;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
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
