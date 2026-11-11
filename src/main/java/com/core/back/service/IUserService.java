package com.core.back.service;

import java.util.List;

import com.core.back.entity.UserEntity;
import com.core.back.payload.UserRequest;

// ASPECTO: Abstracción (Define el contrato puro de negocio para la gestión de usuarios)
public interface IUserService {

    UserRequest createUser(UserRequest userRequest);

    List<UserEntity> getActiveUsers();

    UserRequest getUserByEmail(String email);

    UserRequest updateUser(Long userId, UserRequest userRequest);

    void deleteUser(Long userId);
    
}