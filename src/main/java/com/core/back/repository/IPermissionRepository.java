package com.core.back.repository;

import com.core.back.entity.PermissionEntity;
import java.util.List;
import java.util.Optional;

public interface IPermissionRepository {
    // Métodos existentes
    Optional<PermissionEntity> buscarPorId(Long permisoId);
    List<PermissionEntity> buscarPermisosPorRolId(Long rolId);
    void asignarPermisoARol(Long rolId, Long permisoId);
    void removerPermisoDeRol(Long rolId, Long permisoId);
    void eliminarPermisosDeRol(Long rolId);
    // Nuevos métodos para el CRUD de Permisos
    PermissionEntity guardar(PermissionEntity permiso);
    void actualizar(PermissionEntity permiso);
    void eliminar(Long permisoId);
    List<PermissionEntity> obtenerTodos();
}