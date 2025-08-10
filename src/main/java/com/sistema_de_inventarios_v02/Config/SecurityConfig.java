package com.sistema_de_inventarios_v02.Config;

import com.sistema_de_inventarios_v02.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_VISITOR = "VISITOR";
    private static final String ROLES_CLAIM = "roles";

    private static final String ADMIN = "hasRole('ADMIN')";
    private static final String ADMIN_OR_USER = "hasRole('ADMIN') or hasRole('USER')";
    private static final String ALL_ROLES = "hasRole('ADMIN') or hasRole('USER') or hasRole('VISITOR')";

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
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico")
                        .permitAll()
                        .requestMatchers("/static/**", "/webjars/**", "/resources/**")
                        .permitAll()
                        .requestMatchers("/login", "/logout", "/custom-logout", "/error")
                        .permitAll()
                        .requestMatchers("/api/auth/**")
                        .permitAll()
                        .requestMatchers("/api/debug/**")
                        .permitAll()
                        .requestMatchers("/api/inventory/**")
                        .authenticated()
                        .requestMatchers("/admin/**")
                        .hasRole(ROLE_ADMIN)
                        .requestMatchers("/productos/crear", "/productos/editar/**", "/productos/eliminar/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers("/categorias/crear", "/categorias/editar/**", "/categorias/eliminar/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers("/usuarios/**")
                        .hasRole(ROLE_ADMIN)
                        .requestMatchers("/reportes/admin/**")
                        .hasRole(ROLE_ADMIN)
                        .requestMatchers("/api/admin/**")
                        .hasRole(ROLE_ADMIN)
                        .requestMatchers("/productos/actualizar-stock/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers("/inventario/movimientos/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers("/reportes/user/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers("/api/products/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER, ROLE_VISITOR)
                        .requestMatchers("/productos/ver/**", "/categorias/ver/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER, ROLE_VISITOR)
                        .requestMatchers("/inventario/consultar/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER, ROLE_VISITOR)
                        .requestMatchers("/reportes/basicos/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER, ROLE_VISITOR)
                        .requestMatchers("/catalog")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER, ROLE_VISITOR)
                        .requestMatchers("/", "/dashboard", "/perfil/**", "/products", "/profile")
                        .authenticated()
                        .anyRequest()
                        .authenticated()
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
        public void logout(HttpServletRequest request, HttpServletResponse response,
                           Authentication authentication) {
            if (logger.isInfoEnabled()) {
                logger.info("Iniciando proceso de logout completo con Keycloak");
            }

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

                if (logger.isInfoEnabled()) {
                    logger.info("Redirigiendo a Keycloak logout: {}", logoutUrl);
                }

                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");

                try {
                    response.sendRedirect(logoutUrl);
                } catch (IOException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error al redireccionar al logout de Keycloak", e);
                    }
                    redirectToLogin(response);
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("No hay usuario OIDC para logout de Keycloak, solo logout local");
                }
                redirectToLogin(response);
            }
        }

        private void redirectToLogin(HttpServletResponse response) {
            try {
                response.sendRedirect("/login?logout=true");
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error al redireccionar después del logout", e);
                }
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
                if (logger.isDebugEnabled()) {
                    logger.debug("Limpiando cookie de Keycloak: {}", cookieName);
                }
            }

            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie existingCookie : cookies) {
                    String cookieName = existingCookie.getName();
                    if (cookieName.startsWith("KEYCLOAK") ||
                            cookieName.startsWith("KC_") ||
                            cookieName.startsWith("AUTH_")) {

                        Cookie deleteCookie = new Cookie(cookieName, null);
                        deleteCookie.setPath("/");
                        deleteCookie.setMaxAge(0);
                        deleteCookie.setHttpOnly(true);
                        deleteCookie.setSecure(false);
                        response.addCookie(deleteCookie);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Eliminando cookie existente: {}", cookieName);
                        }
                    }
                }
            }
        }
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            if (logger.isInfoEnabled()) {
                logger.info("Starting role mapping process");
            }

            for (GrantedAuthority authority : authorities) {
                if (logger.isInfoEnabled()) {
                    logger.info("Processing authority: {}", authority.getAuthority());
                }

                if (authority instanceof OidcUserAuthority) {
                    processOidcUserAuthority((OidcUserAuthority) authority, mappedAuthorities);
                }

                if (authority instanceof OAuth2UserAuthority) {
                    processOAuth2UserAuthority((OAuth2UserAuthority) authority, mappedAuthorities);
                }
            }

            if (mappedAuthorities.isEmpty()) {
                if (logger.isWarnEnabled()) {
                    logger.warn("No application roles found, assigning VISITOR by default");
                }
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + ROLE_VISITOR));
            }

            if (logger.isInfoEnabled()) {
                logger.info("Final mapped authorities: {}", mappedAuthorities);
            }
            return mappedAuthorities;
        };
    }

    private void processOidcUserAuthority(OidcUserAuthority oidcUserAuthority,
                                          Set<GrantedAuthority> mappedAuthorities) {
        OidcIdToken idToken = oidcUserAuthority.getIdToken();

        if (logger.isInfoEnabled()) {
            logger.info("Processing OIDC User Authority");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("ID Token claims: {}", idToken.getClaims());
        }

        processRealmRoles(idToken.getClaim("realm_access"), mappedAuthorities);
        processClientRoles(idToken.getClaim("resource_access"), mappedAuthorities);
    }

    private void processOAuth2UserAuthority(OAuth2UserAuthority oauth2Authority,
                                            Set<GrantedAuthority> mappedAuthorities) {
        Map<String, Object> attributes = oauth2Authority.getAttributes();
        if (logger.isInfoEnabled()) {
            logger.info("Processing OAuth2 User Authority with attributes: {}", attributes.keySet());
        }

        if (attributes.containsKey("realm_access")) {
            processRealmRoles(attributes.get("realm_access"), mappedAuthorities);
        }
    }

    @SuppressWarnings("unchecked")
    private void processRealmRoles(Object realmAccessObj, Set<GrantedAuthority> mappedAuthorities) {
        if (realmAccessObj instanceof Map) {
            Map<String, Object> realmAccess = (Map<String, Object>) realmAccessObj;
            if (realmAccess.containsKey(ROLES_CLAIM)) {
                List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);
                if (logger.isInfoEnabled()) {
                    logger.info("Roles from realm_access: {}", roles);
                }

                if (roles != null) {
                    for (String role : roles) {
                        if (isApplicationRole(role)) {
                            SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + role);
                            mappedAuthorities.add(grantedAuthority);
                            if (logger.isInfoEnabled()) {
                                logger.info("Added realm authority: {}", grantedAuthority.getAuthority());
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processClientRoles(Object resourceAccessObj, Set<GrantedAuthority> mappedAuthorities) {
        if (resourceAccessObj instanceof Map) {
            Map<String, Object> resourceAccess = (Map<String, Object>) resourceAccessObj;
            Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get("inventory-system");
            if (clientResource != null && clientResource.containsKey(ROLES_CLAIM)) {
                List<String> clientRoles = (List<String>) clientResource.get(ROLES_CLAIM);
                if (logger.isInfoEnabled()) {
                    logger.info("Client roles from resource_access: {}", clientRoles);
                }

                if (clientRoles != null) {
                    for (String role : clientRoles) {
                        if (isApplicationRole(role)) {
                            SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + role);
                            mappedAuthorities.add(grantedAuthority);
                            if (logger.isInfoEnabled()) {
                                logger.info("Added client authority: {}", grantedAuthority.getAuthority());
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isApplicationRole(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_USER.equals(role) || ROLE_VISITOR.equals(role);
    }
}