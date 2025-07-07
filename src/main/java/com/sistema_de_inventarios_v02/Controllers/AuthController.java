package com.sistema_de_inventarios_v02.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            logger.info("User authenticated: {}", authentication.getName());

            String username = authentication.getName();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            logger.info("User: {} has authorities: {}", username, authorities);

            model.addAttribute("username", username);
            model.addAttribute("authorities", authorities);

            String userRole = determineUserRole(authorities);
            model.addAttribute("userRole", userRole);

            if (authentication.getPrincipal() instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                model.addAttribute("email", oidcUser.getEmail());
                model.addAttribute("fullName", oidcUser.getFullName());

                Map<String, Object> claims = oidcUser.getClaims();
                model.addAttribute("givenName", claims.get("given_name"));
                model.addAttribute("familyName", claims.get("family_name"));
            }

            logger.info("Redirecting user {} with role {} to dashboard", username, userRole);
            return "index";
        }

        logger.warn("User not authenticated, redirecting to login");
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            logger.info("Authenticated user accessing home, redirecting to dashboard");
            return "redirect:/dashboard";
        }
        logger.info("Unauthenticated user accessing home, redirecting to login");
        return "redirect:/login";
    }

    @GetMapping("/error")
    public String error(Model model) {
        model.addAttribute("errorMessage", "Ha ocurrido un error");
        return "error";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String userRole = determineUserRole(authentication.getAuthorities());

            model.addAttribute("username", username);
            model.addAttribute("userRole", userRole);
            model.addAttribute("errorMessage", "No tienes permisos para acceder a esta página");

            logger.warn("Access denied for user: {} with role: {}", username, userRole);
        }
        return "access-denied";
    }

    private String determineUserRole(Collection<? extends GrantedAuthority> authorities) {
        if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "ADMIN";
        } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
            return "USER";
        } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_VISITOR"))) {
            return "VISITOR";
        }

        logger.warn("No recognized role found in authorities: {}", authorities);
        return "VISITOR";
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
        return authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
}