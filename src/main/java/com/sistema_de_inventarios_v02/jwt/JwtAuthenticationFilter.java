package com.sistema_de_inventarios_v02.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ANONYMOUS_USER = "anonymousUser";
    private static final String ROLE_PREFIX = "ROLE_";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (!isCustomJWTEndpoint(requestUri)) {
            logger.debug("Skipping JWT processing for Keycloak endpoint: {}", requestUri);
            chain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !ANONYMOUS_USER.equals(SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal())) {

            logger.debug("Clearing OAuth2 authentication for JWT endpoint: {}", requestUri);
            SecurityContextHolder.getContext().setAuthentication(null);
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;
        String role = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith(BEARER_PREFIX)) {
            jwtToken = requestTokenHeader.substring(BEARER_PREFIX.length());
            try {
                if (isKeycloakToken(jwtToken)) {
                    logger.error("Keycloak token detected on JWT endpoint: {}", requestUri);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"WRONG_TOKEN_TYPE\",\"message\":\"Este endpoint requiere JWT personalizado, no token de Keycloak\"}");
                    return;
                }

                username = jwtUtil.extractUsername(jwtToken);
                role = jwtUtil.extractRole(jwtToken);

                logger.debug("Extracted from custom JWT - Username: {}, Role: {}", username, role);

            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token: {}", e.getMessage());
                sendUnauthorizedResponse(response, "TOKEN_INVALID", "Token JWT inválido");
                return;
            } catch (Exception e) {
                logger.error("JWT Token has expired or is invalid: {}", e.getMessage());
                sendUnauthorizedResponse(response, "TOKEN_EXPIRED", "Token JWT expirado o inválido");
                return;
            }
        } else {
            logger.error("No JWT Token found for API endpoint: {}", requestUri);
            sendUnauthorizedResponse(response, "TOKEN_REQUIRED", "Token JWT requerido para este endpoint");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(jwtToken)) {

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(ROLE_PREFIX + role);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null,
                                Collections.singletonList(authority));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.debug("JWT authentication successful for user: {} with role: {}", username, role);
            } else {
                logger.warn("JWT token validation failed for user: {}", username);
                sendUnauthorizedResponse(response, "TOKEN_VALIDATION_FAILED", "Validación de token falló");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isCustomJWTEndpoint(String path) {
        return path.startsWith("/api/auth/") || path.startsWith("/api/inventory/");
    }

    private boolean isKeycloakToken(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                return false;
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));

            return payload.contains("realm_access") ||
                    payload.contains("resource_access") ||
                    payload.contains("localhost:8080/realms/inventory-realm") ||
                    payload.contains("inventory-system");

        } catch (Exception e) {
            logger.debug("Error analyzing token structure: {}", e.getMessage());
            return false;
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String errorCode, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                errorCode, message, java.time.LocalDateTime.now()
        ));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        boolean shouldSkip = path.startsWith("/login") ||
                path.startsWith("/oauth2") ||
                path.startsWith("/error") ||
                path.startsWith("/logout") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                path.startsWith("/assets/") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/fonts/") ||
                "/favicon.ico".equals(path) ||
                "/".equals(path) ||

                "/api/auth/login".equals(path) ||
                "/api/auth/register".equals(path);

        if (shouldSkip && logger.isDebugEnabled()) {
            logger.debug("Skipping JWT filter for public/static path: {}", path);
        }

        return shouldSkip;
    }
}