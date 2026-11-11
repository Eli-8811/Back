package com.core.back.entity;

public class PermissionEntity extends BaseEntity {

    private Long permissionId;
    private String name;
    private String perspective;

    public PermissionEntity() {
    }

    public PermissionEntity(Long permissionId, String name, String perspective) {
        this.permissionId = permissionId;
        this.name = name;
        this.perspective = perspective;
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

}