package com.core.back.controller;

import com.core.back.payload.AssignPermissionsRequest;
import com.core.back.payload.GenericResponse;
import com.core.back.payload.RoleRequest;
import com.core.back.service.IRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0/role")
public class RoleController {

    private final IRoleService roleService;

    public RoleController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('CREATE_ROL')")
    public ResponseEntity<GenericResponse<Object>> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>(true, "Role created successfully", roleService.saveRole(roleRequest)));
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and hasAuthority('READ_ROL')")
    public ResponseEntity<GenericResponse<Object>> getAllRoles() {
        return ResponseEntity.ok(new GenericResponse<>(true, "Role list retrieved successfully", roleService.getAllRoles()));
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('UPDATE_ROL')")
    public ResponseEntity<GenericResponse<Object>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest roleRequest) {
        return ResponseEntity.ok(new GenericResponse<>(true, "Role updated successfully", roleService.updateRole(id, roleRequest)));
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('DELETE_ROL')")
    public ResponseEntity<GenericResponse<Object>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(new GenericResponse<>(true, "Role deleted successfully", null));
    }
 
    @PostMapping("/{id}/permission")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('UPDATE_ROL')")
    public ResponseEntity<GenericResponse<Object>> assignPermissionsToRole(@PathVariable Long id, @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissionsToRole(id, request.getPermissionIds());
        return ResponseEntity.ok(new GenericResponse<>(true, "Permissions assigned successfully", null));
    }
    
}