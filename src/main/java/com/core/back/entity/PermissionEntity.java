package com.core.back.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionEntity extends BaseEntity {
    private Long permisoId;
    private String nombre;
    private String perspectiva;
}