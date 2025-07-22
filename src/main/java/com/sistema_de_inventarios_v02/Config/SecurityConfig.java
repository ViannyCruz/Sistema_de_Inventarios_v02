package com.sistema_de_inventarios_v02.Config;

import com.sistema_de_inventarios_v02.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${keycloak.logout-url:http://localhost:8080/realms/inventory-realm/protocol/openid-connect/logout}")
    private String keycloakLogoutUrl;

    @Value("${app.base-url:http://localhost:8081}")
    private String appBaseUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/custom-logout")
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico").permitAll()
                        .requestMatchers("/static/**", "/webjars/**", "/resources/**").permitAll()

                        .requestMatchers("/login", "/logout", "/custom-logout", "/error").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/api/debug/**").permitAll()

                        .requestMatchers("/api/inventory/**").authenticated()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/productos/crear", "/productos/editar/**", "/productos/eliminar/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/categorias/crear", "/categorias/editar/**", "/categorias/eliminar/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/reportes/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers("/productos/actualizar-stock/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/inventario/movimientos/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/reportes/user/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "USER", "VISITOR")

                        .requestMatchers("/productos/ver/**", "/categorias/ver/**").hasAnyRole("ADMIN", "USER", "VISITOR")
                        .requestMatchers("/inventario/consultar/**").hasAnyRole("ADMIN", "USER", "VISITOR")
                        .requestMatchers("/reportes/basicos/**").hasAnyRole("ADMIN", "USER", "VISITOR")
                        .requestMatchers("/catalog").hasAnyRole("ADMIN", "USER", "VISITOR")

                        .requestMatchers("/", "/dashboard", "/perfil/**", "/products", "/profile").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(this.userAuthoritiesMapper())
                        )
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .addLogoutHandler(cookieClearingLogoutHandler())
                        .addLogoutHandler(keycloakLogoutHandler())
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "SESSION", "KEYCLOAK_SESSION", "KEYCLOAK_IDENTITY")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .and()
                        .sessionFixation().changeSessionId()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/error?type=access-denied")
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public LogoutHandler cookieClearingLogoutHandler() {
        return new CookieClearingLogoutHandler(
                "JSESSIONID",
                "SESSION",
                "KEYCLOAK_SESSION",
                "KEYCLOAK_IDENTITY",
                "KEYCLOAK_SESSION_LEGACY",
                "KC_RESTART",
                "AUTH_SESSION_ID",
                "AUTH_SESSION_ID_LEGACY"
        );
    }

    @Bean
    public LogoutHandler keycloakLogoutHandler() {
        return new KeycloakLogoutHandler();
    }

    private class KeycloakLogoutHandler implements LogoutHandler {

        @Override
        public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
            logger.info("Iniciando proceso de logout completo con Keycloak");

            clearKeycloakCookies(request, response);

            SecurityContextLogoutHandler localLogoutHandler = new SecurityContextLogoutHandler();
            localLogoutHandler.logout(request, response, authentication);

            if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                String idToken = oidcUser.getIdToken().getTokenValue();

                String postLogoutRedirectUri = appBaseUrl + "/login";

                String logoutUrl = UriComponentsBuilder
                        .fromUriString(keycloakLogoutUrl)
                        .queryParam("id_token_hint", idToken)
                        .queryParam("post_logout_redirect_uri", postLogoutRedirectUri)
                        .build()
                        .toUriString();

                logger.info("Redirigiendo a Keycloak logout: {}", logoutUrl);

                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");

                try {
                    response.sendRedirect(logoutUrl);
                } catch (IOException e) {
                    logger.error("Error al redireccionar al logout de Keycloak", e);
                    redirectToLogin(response);
                }
            } else {
                logger.info("No hay usuario OIDC para logout de Keycloak, solo logout local");
                redirectToLogin(response);
            }
        }

        private void redirectToLogin(HttpServletResponse response) {
            try {
                response.sendRedirect("/login?logout=true");
            } catch (IOException e) {
                logger.error("Error al redireccionar después del logout", e);
            }
        }

        private void clearKeycloakCookies(HttpServletRequest request, HttpServletResponse response) {
            String[] keycloakCookies = {
                    "KEYCLOAK_SESSION",
                    "KEYCLOAK_IDENTITY",
                    "KEYCLOAK_SESSION_LEGACY",
                    "KC_RESTART",
                    "AUTH_SESSION_ID",
                    "AUTH_SESSION_ID_LEGACY",
                    "KEYCLOAK_LOCALE"
            };

            for (String cookieName : keycloakCookies) {
                Cookie cookie = new Cookie(cookieName, null);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                response.addCookie(cookie);
                logger.debug("Limpiando cookie de Keycloak: {}", cookieName);
            }

            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie existingCookie : cookies) {
                    if (existingCookie.getName().startsWith("KEYCLOAK") ||
                            existingCookie.getName().startsWith("KC_") ||
                            existingCookie.getName().startsWith("AUTH_")) {

                        Cookie deleteCookie = new Cookie(existingCookie.getName(), null);
                        deleteCookie.setPath("/");
                        deleteCookie.setMaxAge(0);
                        deleteCookie.setHttpOnly(true);
                        deleteCookie.setSecure(false);
                        response.addCookie(deleteCookie);
                        logger.debug("Eliminando cookie existente: {}", existingCookie.getName());
                    }
                }
            }
        }
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            logger.info("Starting role mapping process");

            for (GrantedAuthority authority : authorities) {
                logger.info("Processing authority: {}", authority.getAuthority());

                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                    OidcIdToken idToken = oidcUserAuthority.getIdToken();

                    logger.info("Processing OIDC User Authority");
                    logger.debug("ID Token claims: {}", idToken.getClaims());

                    Map<String, Object> realmAccess = idToken.getClaim("realm_access");
                    if (realmAccess != null && realmAccess.containsKey("roles")) {
                        List<String> roles = (List<String>) realmAccess.get("roles");
                        logger.info("Roles from realm_access: {}", roles);

                        if (roles != null) {
                            for (String role : roles) {
                                if (isApplicationRole(role)) {
                                    SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + role);
                                    mappedAuthorities.add(grantedAuthority);
                                    logger.info("Added realm authority: {}", grantedAuthority.getAuthority());
                                }
                            }
                        }
                    }

                    Map<String, Object> resourceAccess = idToken.getClaim("resource_access");
                    if (resourceAccess != null) {
                        Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get("inventory-system");
                        if (clientResource != null && clientResource.containsKey("roles")) {
                            List<String> clientRoles = (List<String>) clientResource.get("roles");
                            logger.info("Client roles from resource_access: {}", clientRoles);

                            if (clientRoles != null) {
                                for (String role : clientRoles) {
                                    if (isApplicationRole(role)) {
                                        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + role);
                                        mappedAuthorities.add(grantedAuthority);
                                        logger.info("Added client authority: {}", grantedAuthority.getAuthority());
                                    }
                                }
                            }
                        }
                    }
                }

                if (authority instanceof OAuth2UserAuthority) {
                    OAuth2UserAuthority oauth2Authority = (OAuth2UserAuthority) authority;
                    Map<String, Object> attributes = oauth2Authority.getAttributes();
                    logger.info("Processing OAuth2 User Authority with attributes: {}", attributes.keySet());

                    if (attributes.containsKey("realm_access")) {
                        Map<String, Object> realmAccess = (Map<String, Object>) attributes.get("realm_access");
                        if (realmAccess != null && realmAccess.containsKey("roles")) {
                            List<String> roles = (List<String>) realmAccess.get("roles");
                            for (String role : roles) {
                                if (isApplicationRole(role)) {
                                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                                    logger.info("Added OAuth2 authority: ROLE_{}", role);
                                }
                            }
                        }
                    }
                }
            }

            if (mappedAuthorities.isEmpty()) {
                logger.warn("No application roles found, assigning VISITOR by default");
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_VISITOR"));
            }

            logger.info("Final mapped authorities: {}", mappedAuthorities);
            return mappedAuthorities;
        };
    }

    private boolean isApplicationRole(String role) {
        return role != null && (role.equals("ADMIN") || role.equals("USER") || role.equals("VISITOR"));
    }
}