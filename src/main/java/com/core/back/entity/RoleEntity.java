package com.core.back.entity;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity extends BaseEntity {
    private Long rolId;
    private String nombre;
    private String perspectiva;    
    private List<PermissionEntity> permisos;
}