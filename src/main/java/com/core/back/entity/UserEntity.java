package com.core.back.entity;

import lombok.*;
import java.util.List;

// ASPECTO: Clases y Objetos (Definición de una Clase Modelo/POJO)
// ASPECTO: Herencia (UserEntity HEREDA de BaseEntity)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {

    private Long usuarioId;
    private String nombre;
    private String apellidos;
    private Integer edad;
    private Integer genero;
    private String username; // Requerido por Spring Security
    private String password; // Requerido por Spring Security
    private String correo;

    // Relación lógica para el mapeo manual con JdbcTemplate
    private List<RoleEntity> roles;

}