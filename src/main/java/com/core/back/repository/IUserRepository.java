package com.core.back.repository;

import com.core.back.entity.UserEntity;
import java.util.Optional;
import java.util.List;

public interface IUserRepository {

    UserEntity saveUser(UserEntity user);

    Optional<UserEntity> findUserByEmail(String email);

    Optional<UserEntity> findUserDataByUsername(String username);

    boolean existsUserByEmail(String email);

    boolean existsUserByUsername(String username);

    boolean existsRoleById(Long roleId);

    void assignRoleToUser(Long userId, Long roleId);

}