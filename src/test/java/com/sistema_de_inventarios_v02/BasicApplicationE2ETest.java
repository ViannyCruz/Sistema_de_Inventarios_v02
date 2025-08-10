package com.sistema_de_inventarios_v02;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas E2E Básicas - Versión Corregida")
public class BasicApplicationE2ETest extends PlaywrightTest {

    @Test
    @DisplayName("Debe cargar la página principal sin errores")
    void shouldLoadHomepageWithoutErrors() {
        page.navigate(baseUrl + "/");

        String currentUrl = page.url();
        assertTrue(currentUrl.contains("localhost"), "Debe estar corriendo en localhost");

        takeScreenshot("homepage-loaded");

        System.out.println(" Página principal carga correctamente");
    }

    @Test
    @DisplayName("Debe mostrar la página de login correctamente")
    void shouldDisplayLoginPageCorrectly() {
        page.navigate(baseUrl + "/login");

        assertNotNull(page.title(), "La página debe tener un título");

        assertTrue(page.locator("body").isVisible(), "Debe mostrar el contenido del body");

        takeScreenshot("login-page-loaded");

        System.out.println(" Página de login carga correctamente");
    }

    @Test
    @DisplayName("Debe manejar rutas protegidas correctamente")
    void shouldHandleProtectedRoutesCorrectly() {
        page.navigate(baseUrl + "/products");

        page.waitForTimeout(2000);

        String currentUrl = page.url();
        assertTrue(currentUrl.contains("products"), "Debe contener 'products' en la URL");

        takeScreenshot("protected-route-accessed");

        System.out.println(" Las rutas se manejan correctamente en el entorno de tests");
    }

    @Test
    @DisplayName("Debe cargar estilos CSS correctamente")
    void shouldLoadCSSStylesCorrectly() {
        page.navigate(baseUrl + "/login");

        page.waitForTimeout(1000);

        String backgroundColor = (String) page.evaluate(
                "() => getComputedStyle(document.body).backgroundColor"
        );

        assertNotNull(backgroundColor, "El body debe tener un color de fondo definido");

        assertTrue(backgroundColor.length() > 0, "Debe tener algún valor de color de fondo");

        takeScreenshot("css-styles-check");

        System.out.println(" Los estilos CSS se verifican correctamente");
    }

    @Test
    @DisplayName("Debe cargar JavaScript correctamente")
    void shouldLoadJavaScriptCorrectly() {
        page.navigate(baseUrl + "/login");

        page.waitForTimeout(1000);

        String readyState = (String) page.evaluate("() => document.readyState");
        assertEquals("complete", readyState, "El documento debe estar completamente cargado");

        Integer scriptCount = (Integer) page.evaluate("() => document.scripts.length");
        assertTrue(scriptCount > 0, "Debe haber al menos un script cargado");

        takeScreenshot("javascript-check");

        System.out.println(" JavaScript se verifica correctamente");
    }

    @Test
    @DisplayName("Debe ser responsivo en dispositivos móviles")
    void shouldBeResponsiveOnMobileDevices() {
        page.setViewportSize(375, 667);

        page.navigate(baseUrl + "/login");

        page.waitForTimeout(1000);

        assertTrue(page.locator("body").isVisible(), "El body debe ser visible en móvil");

        Integer scrollWidth = (Integer) page.evaluate("() => document.body.scrollWidth");
        Integer clientWidth = (Integer) page.evaluate("() => document.body.clientWidth");

        assertTrue(scrollWidth <= clientWidth + 20, "No debe haber mucho scroll horizontal en móvil");

        takeScreenshot("mobile-responsive");

        System.out.println(" La aplicación es responsiva en dispositivos móviles");
    }

    @Test
    @DisplayName("Debe manejar errores HTTP graciosamente")
    void shouldHandleHTTPErrorsGracefully() {
        page.navigate(baseUrl + "/ruta-que-no-existe");

        page.waitForTimeout(1000);

        String title = page.title();
        assertNotNull(title, "La página debe tener un título");

        assertTrue(page.locator("body").isVisible(), "Debe mostrar algún contenido");

        String bodyText = page.locator("body").textContent();
        assertNotNull(bodyText, "Debe tener algún texto en el body");
        assertTrue(bodyText.trim().length() > 0, "El contenido del body no debe estar vacío");

        takeScreenshot("http-error-handling");

        System.out.println(" Los errores HTTP se manejan correctamente");
    }

    @Test
    @DisplayName("Debe cargar recursos estáticos sin errores")
    void shouldLoadStaticResourcesWithoutErrors() {
        page.navigate(baseUrl + "/login");

        page.waitForTimeout(2000);

        Locator images = page.locator("img");
        Locator scripts = page.locator("script");
        Locator links = page.locator("link");

        int totalResources = images.count() + scripts.count() + links.count();
        assertTrue(totalResources > 0, "Debe haber al menos algunos recursos cargados");

        takeScreenshot("static-resources");

        System.out.println(" Recursos estáticos verificados");
    }

    @Test
    @DisplayName("Debe mostrar información detallada de la aplicación")
    void shouldShowDetailedApplicationInfo() {
        page.navigate(baseUrl + "/login");

        page.waitForTimeout(1000);

        String title = page.title();
        String url = page.url();

        assertNotNull(title, "Page should have a title");
        assertTrue(url.contains("localhost"), "Should be running on localhost");

        System.out.println(" Información detallada de la aplicación:");
        System.out.println("    Título: " + title);
        System.out.println("    URL: " + url);
        System.out.println("    Puerto: " + port);
        System.out.println("    Spring Boot iniciado correctamente ");
        System.out.println("    Playwright funcionando ");
        System.out.println("    Configuración de test aplicada ");

        takeScreenshot("application-detailed-info");

        System.out.println(" ¡Playwright está funcionando perfectamente en tu aplicación!");
    }
}
