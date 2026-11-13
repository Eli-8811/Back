package com.core.back.controller;

import com.core.back.payload.AssignPermissionsRequest;
import com.core.back.payload.GenericResponse;
import com.core.back.payload.RoleRequest;
import com.core.back.service.IRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0/role")
public class RoleController {

    private final IRoleService roleService;

    public RoleController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER') and hasAuthority('CREATE_ROLE')")
    public ResponseEntity<GenericResponse<Object>> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>(true, "Role created successfully", roleService.saveRole(roleRequest)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER') and hasAuthority('READ_ROLE')")
    public ResponseEntity<GenericResponse<Object>> getAllRoles() {
        return ResponseEntity.ok(new GenericResponse<>(true, "Role list retrieved successfully", roleService.getAllRoles()));
    }

}