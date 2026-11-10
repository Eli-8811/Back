package com.core.back.repository;

import com.core.back.entity.RoleEntity;
import com.core.back.payload.RoleRequest;

import java.util.List;
import java.util.Optional;

public interface IRoleRepository {
    RoleRequest guardarRol(RoleRequest rolDto);
    List<RoleRequest> obtenerTodos();
    RoleRequest actualizarRol(Long id, RoleRequest rolDto);
    void eliminarRol(Long id);
    Optional<RoleEntity> buscarPorId(Long rolId);
    Optional<RoleEntity> buscarPorNombre(String nombre);
    List<RoleEntity> buscarRolesPorUsuarioId(Long usuarioId);
    void asignarRolAUsuario(Long usuarioId, Long rolId);
    void removerRolDeUsuario(Long usuarioId, Long rolId);
}