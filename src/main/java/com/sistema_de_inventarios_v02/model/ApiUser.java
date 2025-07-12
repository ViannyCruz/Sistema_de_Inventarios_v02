package com.sistema_de_inventarios_v02.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "api_users")
public class ApiUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username es requerido")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password es requerido")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Role es requerido")
    @Pattern(regexp = "ADMIN|USER|VISITOR", message = "Role debe ser ADMIN, USER o VISITOR")
    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean enabled = true;

    private String email;
    private String fullName;

    public ApiUser() {}

    public ApiUser(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public ApiUser(String username, String password, String role, String email, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}