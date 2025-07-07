package com.sistema_de_inventarios_v02.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null) {
            response.put("authenticated", true);
            response.put("name", authentication.getName());
            response.put("principal", authentication.getPrincipal().getClass().getSimpleName());

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            response.put("authorities", authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            if (authentication.getPrincipal() instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                response.put("idTokenClaims", oidcUser.getIdToken().getClaims());
                response.put("userAttributes", oidcUser.getAttributes());
            }

            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                response.put("userAttributes", oauth2User.getAttributes());
            }
        } else {
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getRoles(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null) {
            response.put("hasRoleAdmin", authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
            response.put("hasRoleUser", authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
            response.put("hasRoleVisitor", authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_VISITOR")));

            response.put("allAuthorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            response.put("principalClass", authentication.getPrincipal().getClass().getSimpleName());
            response.put("username", authentication.getName());
            response.put("isAuthenticated", authentication.isAuthenticated());
        } else {
            response.put("error", "No authentication found");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> testAdmin(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "¡Acceso ADMIN exitoso!",
                "user", authentication.getName(),
                "role", "ADMIN"
        ));
    }

    @GetMapping("/test-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> testUser(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "¡Acceso USER exitoso!",
                "user", authentication.getName(),
                "role", "USER"
        ));
    }

    @GetMapping("/test-visitor")
    @PreAuthorize("hasRole('VISITOR')")
    public ResponseEntity<Map<String, String>> testVisitor(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "¡Acceso VISITOR exitoso!",
                "user", authentication.getName(),
                "role", "VISITOR"
        ));
    }

    @GetMapping("/test-any-role")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'VISITOR')")
    public ResponseEntity<Map<String, String>> testAnyRole(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority().substring(5))
                .collect(Collectors.joining(", "));

        return ResponseEntity.ok(Map.of(
                "message", "¡Acceso con cualquier rol exitoso!",
                "user", authentication.getName(),
                "roles", role
        ));
    }

    @GetMapping("/keycloak-info")
    public ResponseEntity<Map<String, Object>> getKeycloakInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            Map<String, Object> claims = oidcUser.getIdToken().getClaims();

            response.put("issuer", claims.get("iss"));
            response.put("subject", claims.get("sub"));
            response.put("preferredUsername", claims.get("preferred_username"));
            response.put("email", claims.get("email"));
            response.put("emailVerified", claims.get("email_verified"));
            response.put("name", claims.get("name"));
            response.put("givenName", claims.get("given_name"));
            response.put("familyName", claims.get("family_name"));

            response.put("realmAccess", claims.get("realm_access"));
            response.put("resourceAccess", claims.get("resource_access"));

            response.put("authTime", claims.get("auth_time"));
            response.put("sessionId", claims.get("sid"));
            response.put("audience", claims.get("aud"));
            response.put("azp", claims.get("azp"));
        } else {
            response.put("error", "No OIDC user found");
        }

        return ResponseEntity.ok(response);
    }
}