package com.core.back.controller;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.PermissionRequest;
import com.core.back.service.IPermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/permisos")
public class PermissionController {

    private final IPermissionService permissionService;

    public PermissionController(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('CREATE_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> createPermission(@Valid @RequestBody PermissionRequest permissionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericResponse<>(true, "Permission created successfully", permissionService.savePermission(permissionRequest)));
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and hasAuthority('READ_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> getAllPermissions() {
        return ResponseEntity.ok(new GenericResponse<>(true, "Permission list retrieved successfully", permissionService.getAllPermissions()));
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and hasAuthority('READ_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(new GenericResponse<>(true, "Permission found successfully", permissionService.getPermissionById(id)));
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('UPDATE_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest permissionRequest) {
        return ResponseEntity.ok(new GenericResponse<>(true, "Permission updated successfully", permissionService.updatePermission(id, permissionRequest)));
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('DELETE_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(new GenericResponse<>(true, "Permission deleted successfully", null));
    }
    
}