package com.core.back.controller;

import com.core.back.payload.GenericResponse;
import com.core.back.payload.PermissionRequest;
import com.core.back.service.IPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/permisos")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permisoService;

    @PostMapping
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('CREATE_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> crearPermiso(@Valid @RequestBody PermissionRequest permisoDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericResponse<>(true, "Permiso creado exitosamente", permisoService.guardarPermiso(permisoDto)));
    }

    @GetMapping
    //@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and hasAuthority('READ_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> listarPermisos() {
        return ResponseEntity.ok(new GenericResponse<>(true, "Listado de permisos", permisoService.obtenerTodos()));
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') and hasAuthority('READ_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new GenericResponse<>(true, "Permiso encontrado", permisoService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('UPDATE_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> actualizarPermiso(@PathVariable Long id, @Valid @RequestBody PermissionRequest permisoDto) {
        return ResponseEntity.ok(new GenericResponse<>(true, "Permiso actualizado", permisoService.actualizarPermiso(id, permisoDto)));
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('SUPER_ADMIN') and hasAuthority('DELETE_PERMISO')")
    public ResponseEntity<GenericResponse<Object>> eliminarPermiso(@PathVariable Long id) {
        permisoService.eliminarPermiso(id);
        return ResponseEntity.ok(new GenericResponse<>(true, "Permiso eliminado correctamente", null));
    }
    
}