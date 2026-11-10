package com.core.back.controller;

import com.core.back.payload.AssignPermissionsRequest;
import com.core.back.payload.GenericResponse;
import com.core.back.payload.RoleRequest;
import com.core.back.service.IRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService rolService;

    @PostMapping
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('CREATE_ROL')")
    public ResponseEntity<GenericResponse<Object>> crearRol(@Valid @RequestBody RoleRequest rolDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>(true, "Rol creado exitosamente", rolService.guardarRol(rolDto)));
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and hasAuthority('READ_ROL')")
    public ResponseEntity<GenericResponse<Object>> listarRoles() {
        return ResponseEntity.ok(new GenericResponse<>(true, "Listado de roles", rolService.obtenerTodos()));
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('UPDATE_ROL')")
    public ResponseEntity<GenericResponse<Object>> actualizarRol(@PathVariable Long id, @Valid @RequestBody RoleRequest rolDto) {
        return ResponseEntity.ok(new GenericResponse<>(true, "Rol actualizado", rolService.actualizarRol(id, rolDto)));
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('DELETE_ROL')")
    public ResponseEntity<GenericResponse<Object>> eliminarRol(@PathVariable Long id) {
        rolService.eliminarRol(id);
        return ResponseEntity.ok(new GenericResponse<>(true, "Rol eliminado correctamente", null));
    }
 
    @PostMapping("/{id}/permisos")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('UPDATE_ROL')")
    public ResponseEntity<GenericResponse<Object>> asignarPermisos(@PathVariable Long id, @RequestBody AssignPermissionsRequest request) {
        rolService.asignarPermisosARol(id, request.getPermisoIds());
        return ResponseEntity.ok(new GenericResponse<>(true, "Permisos asignados correctamente", null));
    }
    
}