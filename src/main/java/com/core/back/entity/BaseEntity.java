package com.core.back.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// ASPECTO: Clase Abstracta (No se puede instanciar, define una base común)
@Getter
@Setter
public abstract class BaseEntity {

    // ASPECTO: Métodos/Atributos protegidos (Accesibles por herencia)
	protected Boolean estado;
    protected LocalDateTime fechaCreacion;
    protected LocalDateTime fechaModificacion;
    
}