package com.core.back.service;

import java.util.List;

import com.core.back.entity.UserEntity;
import com.core.back.payload.UserRequest;

// ASPECTO: Abstracción (Define el contrato puro de negocio para la gestión de usuarios)
public interface IUserService {

    UserRequest guardarUsuario(UserRequest usuarioDto);

    List<UserEntity> obtenerUsuariosActivos();

    UserRequest buscarUsuarioPorCorreo(String correo);

    UserRequest actualizarUsuario(Long usuarioId, UserRequest dto);

    void eliminarUsuario(Long usuarioId);
    
}