package com.sistema_de_inventarios_v02.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')")
    public String productsView(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userRole = determineUserRole(authentication);
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            model.addAttribute("userRole", userRole);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("username", authentication.getName());
        }
        return "products";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminView(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        return "admin";
    }

    @GetMapping("/stock-management")
    @PreAuthorize("hasRole('ADMIN')")
    public String stockManagementView(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        return "stock-management";
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public String profileView(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userRole = determineUserRole(authentication);
            model.addAttribute("userRole", userRole);
            model.addAttribute("username", authentication.getName());
        }
        return "profile";
    }

    @GetMapping("/catalog")
    @PreAuthorize("hasRole('VISITOR')")
    public String catalogView(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("userRole", "VISITOR");
        }
        return "catalog";
    }

    @GetMapping("favicon.ico")
    @ResponseBody
    public ResponseEntity<Void> returnNoFavicon() {
        return ResponseEntity.notFound().build();
    }

    private String determineUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse("VISITOR");
    }
}