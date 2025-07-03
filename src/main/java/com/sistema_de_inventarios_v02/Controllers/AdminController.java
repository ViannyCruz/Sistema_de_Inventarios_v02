package com.sistema_de_inventarios_v02.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok("Bienvenido al dashboard de administrador");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok("Lista de usuarios - Solo admin puede ver esto");
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Object userData) {
        return ResponseEntity.ok("Usuario creado exitosamente");
    }
}