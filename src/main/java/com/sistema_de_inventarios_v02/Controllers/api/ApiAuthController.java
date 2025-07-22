package com.sistema_de_inventarios_v02.Controllers.api;

import com.sistema_de_inventarios_v02.dto.api.*;
import com.sistema_de_inventarios_v02.jwt.JwtUtil;
import com.sistema_de_inventarios_v02.model.ApiUser;
import com.sistema_de_inventarios_v02.service.ApiUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ApiAuthController {

    private static final Logger logger = LoggerFactory.getLogger(ApiAuthController.class);

    @Autowired
    private ApiUserService apiUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            logger.info("Attempting to register user: {}", registerRequest.getUsername());

            if (apiUserService.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new ApiErrorResponseDTO(
                                "USERNAME_ALREADY_EXISTS",
                                "El usuario ya existe",
                                "/api/auth/register",
                                LocalDateTime.now()
                        ));
            }

            ApiUser user = new ApiUser();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(registerRequest.getRole());
            user.setEmail(registerRequest.getEmail());
            user.setFullName(registerRequest.getFullName());

            ApiUser savedUser = apiUserService.save(user);

            String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole());

            RegisterResponseDTO response = new RegisterResponseDTO(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getRole(),
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    token,
                    "Usuario registrado exitosamente"
            );

            logger.info("User registered successfully: {}", savedUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponseDTO(
                            "REGISTRATION_ERROR",
                            "Error interno del servidor durante el registro",
                            "/api/auth/register",
                            LocalDateTime.now()
                    ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            logger.info("Attempting to authenticate user: {}", loginRequest.getUsername());

            ApiUser user = apiUserService.findByUsername(loginRequest.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiErrorResponseDTO(
                                "INVALID_CREDENTIALS",
                                "Credenciales inválidas",
                                "/api/auth/login",
                                LocalDateTime.now()
                        ));
            }

            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiErrorResponseDTO(
                                "USER_DISABLED",
                                "Usuario deshabilitado",
                                "/api/auth/login",
                                LocalDateTime.now()
                        ));
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiErrorResponseDTO(
                                "INVALID_CREDENTIALS",
                                "Credenciales inválidas",
                                "/api/auth/login",
                                LocalDateTime.now()
                        ));
            }

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

            LoginResponseDTO response = new LoginResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getEmail(),
                    user.getFullName(),
                    token,
                    "Bearer",
                    86400 // 24 hours in seconds
            );

            logger.info("User authenticated successfully: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponseDTO(
                            "AUTHENTICATION_ERROR",
                            "Error interno del servidor durante la autenticación",
                            "/api/auth/login",
                            LocalDateTime.now()
                    ));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new ApiErrorResponseDTO(
                                "INVALID_TOKEN_FORMAT",
                                "Formato de token inválido",
                                "/api/auth/refresh",
                                LocalDateTime.now()
                        ));
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiErrorResponseDTO(
                                "INVALID_TOKEN",
                                "Token inválido o expirado",
                                "/api/auth/refresh",
                                LocalDateTime.now()
                        ));
            }

            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            ApiUser user = apiUserService.findByUsername(username);
            if (user == null || !user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiErrorResponseDTO(
                                "USER_NOT_FOUND",
                                "Usuario no encontrado o deshabilitado",
                                "/api/auth/refresh",
                                LocalDateTime.now()
                        ));
            }

            String newToken = jwtUtil.generateToken(username, role);

            return ResponseEntity.ok(Map.of(
                    "token", newToken,
                    "type", "Bearer",
                    "expiresIn", 86400
            ));

        } catch (Exception e) {
            logger.error("Error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponseDTO(
                            "TOKEN_REFRESH_ERROR",
                            "Error interno del servidor durante la renovación del token",
                            "/api/auth/refresh",
                            LocalDateTime.now()
                    ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new ApiErrorResponseDTO(
                                "INVALID_TOKEN_FORMAT",
                                "Formato de token inválido",
                                "/api/auth/me",
                                LocalDateTime.now()
                        ));
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiErrorResponseDTO(
                                "INVALID_TOKEN",
                                "Token inválido o expirado",
                                "/api/auth/me",
                                LocalDateTime.now()
                        ));
            }

            String username = jwtUtil.extractUsername(token);
            ApiUser user = apiUserService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiErrorResponseDTO(
                                "USER_NOT_FOUND",
                                "Usuario no encontrado",
                                "/api/auth/me",
                                LocalDateTime.now()
                        ));
            }

            UserInfoResponseDTO response = new UserInfoResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getEmail(),
                    user.getFullName(),
                    user.isEnabled()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting current user info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponseDTO(
                            "USER_INFO_ERROR",
                            "Error interno del servidor al obtener información del usuario",
                            "/api/auth/me",
                            LocalDateTime.now()
                    ));
        }
    }
}