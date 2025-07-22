package com.sistema_de_inventarios_v02.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class LogoutController {

    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    @Value("${keycloak.logout-url:http://localhost:8081/realms/inventory-realm/protocol/openid-connect/logout}")
    private String keycloakLogoutUrl;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @GetMapping("/logout-redirect")
    public void performLogout(HttpServletRequest request, HttpServletResponse response,
                              Authentication authentication) throws IOException {

        logger.info("Iniciando logout manual");
        performCompleteLogout(request, response, authentication);
    }

    @PostMapping("/custom-logout")
    public void customLogout(HttpServletRequest request, HttpServletResponse response,
                             Authentication authentication) throws IOException {

        logger.info("Ejecutando logout personalizado");
        performCompleteLogout(request, response, authentication);
    }

    private void performCompleteLogout(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            logger.info("Sesión local invalidada");
        }

        clearAllCookies(request, response);

        String logoutUrl;

        if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String idToken = oidcUser.getIdToken().getTokenValue();

            logoutUrl = UriComponentsBuilder
                    .fromUriString(keycloakLogoutUrl)
                    .queryParam("id_token_hint", idToken)
                    .queryParam("post_logout_redirect_uri", appBaseUrl + "/login?logout=true")
                    .queryParam("redirect_uri", appBaseUrl + "/login?logout=true")
                    .build()
                    .toUriString();

            logger.info("Redirigiendo a Keycloak logout completo: {}", logoutUrl);
        } else {
            logoutUrl = UriComponentsBuilder
                    .fromUriString(keycloakLogoutUrl)
                    .queryParam("post_logout_redirect_uri", appBaseUrl + "/login?logout=true")
                    .queryParam("redirect_uri", appBaseUrl + "/login?logout=true")
                    .build()
                    .toUriString();

            logger.info("No hay usuario OIDC, logout básico: {}", logoutUrl);
        }

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        response.sendRedirect(logoutUrl);
    }

    private void clearAllCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                Cookie deleteCookie = new Cookie(cookie.getName(), null);
                deleteCookie.setMaxAge(0);
                deleteCookie.setPath("/");
                deleteCookie.setHttpOnly(true);
                deleteCookie.setSecure(false);
                response.addCookie(deleteCookie);

                logger.debug("Eliminando cookie: {}", cookie.getName());
            }
        }

        String[] cookiesToClear = {
                "JSESSIONID",
                "SESSION",
                "KEYCLOAK_SESSION",
                "KEYCLOAK_IDENTITY",
                "KEYCLOAK_SESSION_LEGACY",
                "KC_RESTART",
                "AUTH_SESSION_ID",
                "AUTH_SESSION_ID_LEGACY"
        };

        for (String cookieName : cookiesToClear) {
            Cookie deleteCookie = new Cookie(cookieName, null);
            deleteCookie.setMaxAge(0);
            deleteCookie.setPath("/");
            deleteCookie.setHttpOnly(true);
            deleteCookie.setSecure(false);
            response.addCookie(deleteCookie);

            logger.debug("Forzando eliminación de cookie: {}", cookieName);
        }
    }
}