package com.core.back.payload;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private Long usuarioId;
    @NotBlank private String nombre;
    @NotBlank private String apellidos;
    private Integer edad;
    private Integer genero;
    @NotBlank private String username;
    @NotBlank private String password;
    @Email @NotBlank private String correo;
    private Boolean estado;
    private List<Long> rolesIds; 
}