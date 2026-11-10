package com.core.back.repository;

import com.core.back.entity.UserEntity;
import java.util.Optional;
import java.util.List;

public interface IUserRepository {
    UserEntity guardar(UserEntity usuario);
    UserEntity actualizar(UserEntity usuario);
    Optional<UserEntity> buscarPorId(Long usuarioId);
    Optional<UserEntity> buscarPorCorreo(String correo);
    Optional<UserEntity> buscarPorUsername(String username);
    Optional<List<UserEntity>> obtenerUsuariosActivos(Boolean estado);
    boolean existePorId(Long usuarioId);
    boolean existePorCorreo(String correo);
    boolean existePorUsername(String username);
    void eliminarPorId(Long usuarioId);
    void asignarRol(Long usuarioId, Long rolId);
    boolean existeRolPorId(Long rolId);
}