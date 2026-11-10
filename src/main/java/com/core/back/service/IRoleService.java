package com.core.back.service;

import java.util.List;

import com.core.back.payload.RoleRequest;

public interface IRoleService {
    RoleRequest guardarRol(RoleRequest rolDto);
    List<RoleRequest> obtenerTodos();
    RoleRequest actualizarRol(Long id, RoleRequest rolDto);
    void eliminarRol(Long id);
    void asignarPermisosARol(Long rolId, List<Long> permisoIds);
}