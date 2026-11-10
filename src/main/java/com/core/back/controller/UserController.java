package com.core.back.controller;

import com.core.back.entity.UserEntity;
import com.core.back.payload.GenericResponse;
import com.core.back.payload.UserRequest;
import com.core.back.service.impl.UserServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController // ASPECTO: Patrón de Diseño (Front Controller / MVC)
@RequiredArgsConstructor
@RequestMapping("/v1/usuarios")
public class UserController {

    private final UserServiceImpl usuarioService;

    // === CREAR USUARIO (Retorna 201 Created si todo sale bien) ===
    @PostMapping
    public ResponseEntity<GenericResponse<Object>> crearUsuario(@Valid @RequestBody UserRequest usuarioDto) {
        log.info("REST request para crear un usuario: {}", usuarioDto.getCorreo());
        UserRequest guardado = usuarioService.guardarUsuario(usuarioDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericResponse<>(true, "Usuario creado exitosamente", guardado));
    }
    
    // === OBTENER ACTIVOS (Retorna 200 OK o salta al ExceptionHandler con 404 si está vacía) ===
    @GetMapping
    public ResponseEntity<GenericResponse<Object>> obtenerUsuariosActivos() {
        log.info("REST request para obtener lista completa de usuarios activos");        
        // El service lanzará ResourceNotFoundException si la lista está vacía
        List<UserEntity> usuariosActivos = usuarioService.obtenerUsuariosActivos();        
        return ResponseEntity.ok(new GenericResponse<>(
            true, 
            "Lista de usuarios recuperada exitosamente", 
            usuariosActivos
        ));
    }

    // === OBTENER POR CORREO (Retorna 200 OK o salta al ExceptionHandler con 404 si no existe) ===
    @GetMapping("/correo/{correo}")
    public ResponseEntity<GenericResponse<Object>> obtenerUsuarioPorCorreo(@PathVariable String correo) {
        log.info("REST request para obtener usuario por correo: {}", correo);
        UserRequest usuario = usuarioService.buscarUsuarioPorCorreo(correo);
        
        return ResponseEntity.ok(new GenericResponse<>(true, "Usuario encontrado exitosamente", usuario));
    }

    // === ACTUALIZAR USUARIO (Retorna 200 OK o arroja 404/409 según las reglas del Service) ===
    @PutMapping("/{usuarioId}")
    public ResponseEntity<GenericResponse<Object>> actualizarUsuario(@PathVariable Long usuarioId, @Valid @RequestBody UserRequest usuarioDto) {
        log.info("REST request para actualizar usuario ID: {}", usuarioId);
        UserRequest usuarioActualizado = usuarioService.actualizarUsuario(usuarioId, usuarioDto);
        
        return ResponseEntity.ok(new GenericResponse<>(true, "Usuario actualizado exitosamente", usuarioActualizado));
    }

    // === ELIMINAR USUARIO (Retorna 200 OK o arroja 404 si el ID no existe) ===
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<GenericResponse<Object>> eliminarUsuario(@PathVariable Long usuarioId) {
        log.info("REST request para eliminar usuario ID: {}", usuarioId);
        usuarioService.eliminarUsuario(usuarioId);
        
        return ResponseEntity.ok(new GenericResponse<>(true, "Usuario eliminado exitosamente", null));
    }
    
}