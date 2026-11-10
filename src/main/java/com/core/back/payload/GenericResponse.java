package com.core.back.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//ASPECTO: Generics (El uso de <T> permite que esta clase transporte cualquier tipo de objeto)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericResponse<T> {
	
    private boolean success; 
    private String message;
    
    private T data; // ASPECTO: Atributo Genérico

    // ASPECTO: Método Estático y Genérico
    public static <T> GenericResponse<T> ok(String message, T data) {
        return new GenericResponse<>(true, message, data);
    }

    // ASPECTO: Método Estático y Genérico
    public static <T> GenericResponse<T> error(String message) {
        return new GenericResponse<>(false, message, null);
    }
    
}