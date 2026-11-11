package com.core.back.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignInRequest {
    
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    private String username;
    
    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}