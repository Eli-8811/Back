package com.core.back.controller;

import com.core.back.entity.UserEntity;
import com.core.back.payload.GenericResponse;
import com.core.back.payload.UserRequest;
import com.core.back.service.IUserService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0/user")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<GenericResponse<Object>> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserRequest uR = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>(true, "User created successfully", uR));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<GenericResponse<Object>> getUserDataByUsername(@PathVariable String username) {
        UserRequest user = userService.getUserDataByUsername(username);
        return ResponseEntity.ok(new GenericResponse<>(true, "User found successfully", user));
    }

}