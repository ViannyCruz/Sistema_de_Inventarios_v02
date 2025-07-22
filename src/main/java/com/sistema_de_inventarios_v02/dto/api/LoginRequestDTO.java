package com.sistema_de_inventarios_v02.dto.api;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class LoginRequestDTO {
    @NotBlank(message = "Username es requerido")
    private String username;

    @NotBlank(message = "Password es requerido")
    private String password;

    // Constructors
    public LoginRequestDTO() {}

    public LoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
