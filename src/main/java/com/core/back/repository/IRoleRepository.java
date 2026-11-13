package com.core.back.repository;

import com.core.back.entity.RoleEntity;
import com.core.back.payload.RoleRequest;

import java.util.List;
import java.util.Optional;

public interface IRoleRepository {

    RoleRequest saveRole(RoleRequest roleRequest);

    List<RoleRequest> findAllRoles();

}