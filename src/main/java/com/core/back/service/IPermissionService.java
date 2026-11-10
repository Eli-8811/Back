package com.core.back.service;

import com.core.back.payload.PermissionRequest;
import java.util.List;

public interface IPermissionService {
    
    PermissionRequest guardarPermiso(PermissionRequest permisoDto);
    
    List<PermissionRequest> obtenerTodos();
    
    PermissionRequest buscarPorId(Long id);
    
    PermissionRequest actualizarPermiso(Long id, PermissionRequest permisoDto);
    
    void eliminarPermiso(Long id);
    
}