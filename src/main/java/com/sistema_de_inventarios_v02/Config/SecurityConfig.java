package com.sistema_de_inventarios_v02.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login", "/logout", "/error").permitAll()
                        .requestMatchers("/api/debug/**").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/productos/crear", "/productos/editar/**", "/productos/eliminar/**").hasRole("ADMIN")
                        .requestMatchers("/categorias/crear", "/categorias/editar/**", "/categorias/eliminar/**").hasRole("ADMIN")
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
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/error?type=access-denied")
                );

        return http.build();
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