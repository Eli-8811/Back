package com.core.back.service.impl;

import com.core.back.exception.ResourceNotFoundException;
import com.core.back.payload.RoleRequest;
import com.core.back.repository.IPermissionRepository;
import com.core.back.repository.IRoleRepository;
import com.core.back.service.IRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements IRoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final IRoleRepository roleRepository;

    public RoleServiceImpl(IRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleRequest saveRole(RoleRequest roleRequest) {
        RoleRequest savedRole = roleRepository.saveRole(roleRequest);
        log.info("Role created with ID {}", savedRole.getRoleId());
        return savedRole;
    }

    @Override
    public List<RoleRequest> getAllRoles() {
        return roleRepository.findAllRoles();
    }

}