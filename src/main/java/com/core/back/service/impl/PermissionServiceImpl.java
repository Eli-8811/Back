package com.core.back.service.impl;

import com.core.back.entity.PermissionEntity;
import com.core.back.exception.ResourceNotFoundException;
import com.core.back.payload.PermissionRequest;
import com.core.back.repository.IPermissionRepository;
import com.core.back.service.IPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements IPermissionService {

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
        return permissionRequest;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionRequest> getAllPermissions() {
        return permissionRepository.findAllPermissions().stream()
                .map(p -> new PermissionRequest(
                        p.getPermissionId(),
                        p.getName(),
                        p.getPerspective(),
                        p.getActive()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionRequest getPermissionById(Long id) {
        return permissionRepository.findPermissionById(id)
                .map(p -> new PermissionRequest(
                        p.getPermissionId(),
                        p.getName(),
                        p.getPerspective(),
                        p.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + id));
    }

    @Override
    @Transactional
    public PermissionRequest updatePermission(Long id, PermissionRequest permissionRequest) {
        PermissionEntity permissionEntity = permissionRepository.findPermissionById(id).orElseThrow(() -> new ResourceNotFoundException("Permission to update not found with ID: " + id));
        permissionEntity.setName(permissionRequest.getName());
        permissionEntity.setPerspective(permissionRequest.getPerspective());
        permissionEntity.setActive(permissionRequest.getActive() != null ? permissionRequest.getActive() : permissionEntity.getActive());
        permissionRepository.updatePermission(permissionEntity);
        permissionRequest.setPermissionId(id);
        return permissionRequest;
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        permissionRepository.findPermissionById(id).orElseThrow(() -> new ResourceNotFoundException("Cannot delete permission: no permission exists with ID " + id));
        permissionRepository.deletePermission(id);
    }
    
}