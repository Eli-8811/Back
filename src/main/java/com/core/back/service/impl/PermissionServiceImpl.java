package com.core.back.service.impl;

import com.core.back.entity.PermissionEntity;
import com.core.back.exception.ResourceNotFoundException;
import com.core.back.payload.PermissionRequest;
import com.core.back.repository.IPermissionRepository;
import com.core.back.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private final IPermissionRepository permisoRepository;

    @Override
    @Transactional
    public PermissionRequest guardarPermiso(PermissionRequest permisoDto) {
        log.info(">>>> [SERVICE] Guardando nuevo permiso: {}", permisoDto.getNombre());
        
        PermissionEntity entidad = new PermissionEntity();
        entidad.setNombre(permisoDto.getNombre());
        entidad.setPerspectiva(permisoDto.getPerspectiva());
        entidad.setEstado(permisoDto.getEstado() != null ? permisoDto.getEstado() : true);
        
        PermissionEntity guardado = permisoRepository.guardar(entidad);
        
        permisoDto.setPermisoId(guardado.getPermisoId());
        return permisoDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionRequest> obtenerTodos() {
        log.debug(">>>> [SERVICE] Listando todos los permisos");
        
        return permisoRepository.obtenerTodos().stream()
                .map(p -> PermissionRequest.builder()
                        .permisoId(p.getPermisoId())
                        .nombre(p.getNombre())
                        .perspectiva(p.getPerspectiva())
                        .estado(p.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionRequest buscarPorId(Long id) {
        return permisoRepository.buscarPorId(id)
                .map(p -> PermissionRequest.builder()
                        .permisoId(p.getPermisoId())
                        .nombre(p.getNombre())
                        .perspectiva(p.getPerspectiva())
                        .estado(p.getEstado())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public PermissionRequest actualizarPermiso(Long id, PermissionRequest permisoDto) {
        log.info(">>>> [SERVICE] Actualizando permiso ID: {}", id);
        
        PermissionEntity existente = permisoRepository.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el permiso para actualizar con ID: " + id));
        
        existente.setNombre(permisoDto.getNombre());
        existente.setPerspectiva(permisoDto.getPerspectiva());
        existente.setEstado(permisoDto.getEstado() != null ? permisoDto.getEstado() : existente.getEstado());
        
        permisoRepository.actualizar(existente);
        
        permisoDto.setPermisoId(id);
        return permisoDto;
    }

    @Override
    @Transactional
    public void eliminarPermiso(Long id) {
        log.info(">>>> [SERVICE] Eliminando permiso ID: {}", id);
        
        permisoRepository.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede eliminar, permiso inexistente con ID: " + id));
        
        permisoRepository.eliminar(id);
    }
    
}