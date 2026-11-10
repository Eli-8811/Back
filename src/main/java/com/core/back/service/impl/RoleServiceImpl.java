package com.core.back.service.impl;

import com.core.back.exception.ResourceNotFoundException;
import com.core.back.payload.RoleRequest;
import com.core.back.repository.IPermissionRepository;
import com.core.back.repository.IRoleRepository;
import com.core.back.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final IRoleRepository rolRepository;
    private final IPermissionRepository permisoRepository;

    @Override
    @Transactional
    public RoleRequest guardarRol(RoleRequest rolDto) {
        log.info(">>>> [SERVICE] Iniciando guardado del rol: {}", rolDto.getNombre());        
        return rolRepository.guardarRol(rolDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleRequest> obtenerTodos() {
        log.debug(">>>> [SERVICE] Consultando todos los roles");
        return rolRepository.obtenerTodos();
    }

    @Override
    @Transactional
    public RoleRequest actualizarRol(Long id, RoleRequest rolDto) {
        log.info(">>>> [SERVICE] Actualizando rol con ID: {}", id);
        
        // Refactorizado con ResourceNotFoundException
        rolRepository.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el rol con ID: " + id));
        
        return rolRepository.actualizarRol(id, rolDto);
    }

    @Override
    @Transactional
    public void eliminarRol(Long id) {
        log.info(">>>> [SERVICE] Eliminando rol con ID: {}", id);        
        
        // Refactorizado con ResourceNotFoundException
        rolRepository.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede eliminar, el rol con ID " + id + " no existe"));
        
        rolRepository.eliminarRol(id);       
        permisoRepository.eliminarPermisosDeRol(id);
    }
    
    @Override
    @Transactional
    public void asignarPermisosARol(Long rolId, List<Long> permisoIds) {
        log.info(">>>> [SERVICE] Asignando {} permisos al rol {}", permisoIds.size(), rolId);
        
        // 1. Validar que el rol existe
        rolRepository.buscarPorId(rolId).orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + rolId));

        // 2. Limpiamos permisos anteriores (Sincronización)
        permisoRepository.eliminarPermisosDeRol(rolId);

        // 3. Insertamos los nuevos
        for (Long pId : permisoIds) {
            permisoRepository.asignarPermisoARol(rolId, pId);
        }
    }
    
}