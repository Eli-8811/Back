package com.core.back.controller;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.PermissionRequest;
import com.core.back.service.IPermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0/permission")
public class PermissionController {

    private final IPermissionService permissionService;

    public PermissionController(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER') and hasAuthority('CREATE_PERMISSION')")
    public ResponseEntity<GenericResponse<Object>> createPermission(@Valid @RequestBody PermissionRequest permissionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericResponse<>(true, "Permission created successfully", permissionService.savePermission(permissionRequest)));
    }
    
}