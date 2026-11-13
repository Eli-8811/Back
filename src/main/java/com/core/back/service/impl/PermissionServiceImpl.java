package com.core.back.service.impl;

import com.core.back.entity.PermissionEntity;
import com.core.back.exception.ResourceNotFoundException;
import com.core.back.payload.PermissionRequest;
import com.core.back.repository.IPermissionRepository;
import com.core.back.service.IPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements IPermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final IPermissionRepository permissionRepository;

    public PermissionServiceImpl(IPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public PermissionRequest savePermission(PermissionRequest permissionRequest) {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setName(permissionRequest.getName());
        permissionEntity.setPerspective(permissionRequest.getPerspective());
        permissionEntity.setActive(permissionRequest.getActive());
        PermissionEntity pE = permissionRepository.savePermission(permissionEntity);
        permissionRequest.setPermissionId(pE.getPermissionId());
        log.info("Permission created with ID {}", pE.getPermissionId());
        return permissionRequest;
    }

}