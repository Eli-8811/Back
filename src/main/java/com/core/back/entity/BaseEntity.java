package com.core.back.entity;

import java.time.LocalDateTime;

// ASPECTO: Clase Abstracta (No se puede instanciar, define una base común)
public abstract class BaseEntity {

    // ASPECTO: Métodos/Atributos protegidos (Accesibles por herencia)
    protected Boolean active;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}