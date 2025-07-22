package com.sistema_de_inventarios_v02.dto.api;

public class LoginResponseDTO {
    private Long id;
    private String username;
    private String role;
    private String email;
    private String fullName;
    private String token;
    private String type;
    private int expiresIn;

    public LoginResponseDTO(Long id, String username, String role, String email,
                            String fullName, String token, String type, int expiresIn) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
        this.token = token;
        this.type = type;
        this.expiresIn = expiresIn;
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
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getExpiresIn() { return expiresIn; }
    public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }
}
