package com.sistema_de_inventarios_v02.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditViewController {

    @GetMapping("/auditoria")
    public String auditPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userRole = "VISITOR"; // Default role
            boolean isAdmin = false;

            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                
                // Obtener roles del usuario
                Object rolesAttr = oauth2User.getAttribute("roles");
                if (rolesAttr != null) {
                    String roles = rolesAttr.toString();
                    if (roles.contains("ADMIN")) {
                        userRole = "ADMIN";
                        isAdmin = true;
                    } else if (roles.contains("USER")) {
                        userRole = "USER";
                    }
                }

                // Agregar información del usuario al modelo
                model.addAttribute("username", oauth2User.getAttribute("preferred_username"));
                model.addAttribute("userEmail", oauth2User.getAttribute("email"));
            }

            model.addAttribute("userRole", userRole);
            model.addAttribute("isAdmin", isAdmin);
        } else {
            model.addAttribute("userRole", "VISITOR");
            model.addAttribute("isAdmin", false);
        }

        return "auditoria"; // Retorna la vista audit.html
    }
}