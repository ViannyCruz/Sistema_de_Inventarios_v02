package com.sistema_de_inventarios_v02.dto.api;

public class UserInfoResponseDTO {
    private Long id;
    private String username;
    private String role;
    private String email;
    private String fullName;
    private boolean enabled;

    public UserInfoResponseDTO(Long id, String username, String role, String email,
                               String fullName, boolean enabled) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
        this.enabled = enabled;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}