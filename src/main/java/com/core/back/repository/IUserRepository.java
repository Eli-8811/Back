package com.core.back.repository;

import com.core.back.entity.UserEntity;
import java.util.Optional;
import java.util.List;

public interface IUserRepository {
    UserEntity saveUser(UserEntity user);
    UserEntity updateUser(UserEntity user);
    Optional<UserEntity> findUserById(Long userId);
    Optional<UserEntity> findUserByEmail(String email);
    Optional<UserEntity> findUserByUsername(String username);
    Optional<UserEntity> findUserDataByUsername(String username);
    Optional<List<UserEntity>> findActiveUsers(Boolean active);
    boolean existsUserById(Long userId);
    boolean existsUserByEmail(String email);
    boolean existsUserByUsername(String username);
    void deleteUserById(Long userId);
    void assignRoleToUser(Long userId, Long roleId);
    boolean existsRoleById(Long roleId);
}