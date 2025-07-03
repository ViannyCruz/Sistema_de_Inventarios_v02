package com.sistema_de_inventarios_v02.Controllers;

import com.sistema_de_inventarios_v02.Util.JwtUtil;
import com.sistema_de_inventarios_v02.dto.LoginRequestDTO;
import com.sistema_de_inventarios_v02.dto.LoginResponseDTO;
import com.sistema_de_inventarios_v02.model.Role;
import com.sistema_de_inventarios_v02.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${app.jwt.expiration}")
    private int jwtExpiration;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        if (adminUsername.equals(loginRequest.getUsername()) &&
                adminPassword.equals(loginRequest.getPassword())) {

            String token = jwtUtil.generateToken(loginRequest.getUsername(), Role.ADMIN);
            tokenBlacklistService.setActiveTokenForUser(loginRequest.getUsername(), token);

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    loginRequest.getUsername(),
                    Role.ADMIN.name(),
                    jwtExpiration
            );

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);

            tokenBlacklistService.removeActiveTokenForUser(username);

            return ResponseEntity.ok("Logout exitoso");
        }
        return ResponseEntity.badRequest().body("Token no encontrado");
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (tokenBlacklistService.isBlacklisted(token)) {
                return ResponseEntity.status(401).body("Token invalidado");
            }

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                return ResponseEntity.ok(new LoginResponseDTO(
                        token, username, role,
                        jwtUtil.getExpirationDateFromToken(token).getTime() - System.currentTimeMillis()
                ));
            }
        }
        return ResponseEntity.status(401).body("Token inválido");
    }
}
