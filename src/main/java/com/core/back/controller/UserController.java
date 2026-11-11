package com.core.back.controller;

import com.core.back.entity.UserEntity;
import com.core.back.payload.GenericResponse;
import com.core.back.payload.UserRequest;
import com.core.back.service.impl.UserServiceImpl;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0/usuarios")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<Object>> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserRequest uR = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>(true, "User created successfully", uR));
    }

    @GetMapping
    public ResponseEntity<GenericResponse<Object>> getActiveUsers() {
        List<UserEntity> users = userService.getActiveUsers();
        return ResponseEntity.ok(new GenericResponse<>(
            true, 
            "User list retrieved successfully",
                users
        ));
    }

    @GetMapping("/mail/{mail}")
    public ResponseEntity<GenericResponse<Object>> getUserByEmail(@PathVariable String mail) {
        UserRequest user = userService.getUserByEmail(mail);
        return ResponseEntity.ok(new GenericResponse<>(true, "User found successfully", user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<GenericResponse<Object>> updateUser(@PathVariable Long userId, @Valid @RequestBody UserRequest userRequest) {
        UserRequest uR = userService.updateUser(userId, userRequest);
        return ResponseEntity.ok(new GenericResponse<>(true, "User updated successfully", uR));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<GenericResponse<Object>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new GenericResponse<>(true, "User deleted successfully", null));
    }
    
}