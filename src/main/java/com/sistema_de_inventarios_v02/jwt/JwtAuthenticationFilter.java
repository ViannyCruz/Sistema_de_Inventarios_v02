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

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !ANONYMOUS_USER.equals(SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal())) {

            if (logger.isDebugEnabled()) {
                logger.debug("OAuth2 authentication already present, skipping JWT processing for: {}",
                        request.getRequestURI());
            }
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;
        String role = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith(BEARER_PREFIX)) {
            jwtToken = requestTokenHeader.substring(BEARER_PREFIX.length());
            try {
                username = jwtUtil.extractUsername(jwtToken);
                role = jwtUtil.extractRole(jwtToken);
            } catch (IllegalArgumentException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to get JWT Token");
                }
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("JWT Token has expired");
                }
            }
        } else {
            String requestUri = request.getRequestURI();
            if (requestUri.startsWith("/api/") &&
                    !requestUri.startsWith("/api/auth/")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No JWT Token found for API endpoint: {}", requestUri);
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(jwtToken)) {

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(ROLE_PREFIX + role);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null,
                                Collections.singletonList(authority));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

                if (logger.isDebugEnabled()) {
                    logger.debug("JWT authentication successful for user: {}", username);
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("JWT token validation failed");
                }
            }
        }
        chain.doFilter(request, response);
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
                path.startsWith("/api/auth/") ||
                "/api/auth/login".equals(path) ||
                "/".equals(path);

        if (shouldSkip && logger.isDebugEnabled()) {
            logger.debug("Skipping JWT filter for path: {}", path);
        }

        return shouldSkip;
    }
}