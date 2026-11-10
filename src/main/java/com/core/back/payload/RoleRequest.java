package com.core.back.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest {

    // ID del rol (se requiere para devolver el ID generado al guardar y para actualizar)
    private Long rolId;

    // Validamos que el nombre no sea nulo ni vacío y tenga un tamaño razonable
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres")
    private String nombre;

    // La perspectiva es opcional, pero limitamos el tamaño
    @Size(max = 255, message = "La perspectiva no debe exceder los 255 caracteres")
    private String perspectiva;

    // Estado lógico del rol (1 = Activo, 0 = Inactivo)
    private Boolean estado;
    
}