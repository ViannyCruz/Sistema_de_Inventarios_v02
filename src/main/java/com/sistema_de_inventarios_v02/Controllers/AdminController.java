package com.sistema_de_inventarios_v02.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
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

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Object roleData) {
        return ResponseEntity.ok("Rol de usuario actualizado exitosamente");
    }

    @GetMapping("/system-config")
    public ResponseEntity<?> getSystemConfig() {
        return ResponseEntity.ok("Configuración del sistema - Solo admin");
    }

    @PutMapping("/system-config")
    public ResponseEntity<?> updateSystemConfig(@RequestBody Object configData) {
        return ResponseEntity.ok("Configuración del sistema actualizada");
    }
}
