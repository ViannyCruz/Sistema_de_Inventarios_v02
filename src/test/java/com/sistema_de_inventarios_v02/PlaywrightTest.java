package com.sistema_de_inventarios_v02;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Paths;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestE2EConfiguration.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "logging.level.org.springframework.security=WARN"
})
public abstract class PlaywrightTest {

    @LocalServerPort
    protected int port;

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected String baseUrl;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();

        boolean headless = Boolean.parseBoolean(System.getProperty("playwright.headless", "true"));
        int slowMo = Integer.parseInt(System.getProperty("playwright.slowmo", "0"));

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(slowMo)
                .setDevtools(!headless);

        String browserType = System.getProperty("playwright.browser", "chromium");

        browser = switch (browserType.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit", "safari" -> playwright.webkit().launch(options);
            default -> playwright.chromium().launch(options);
        };

        System.out.println("Playwright iniciado con navegador: " + browserType +
                " (headless: " + headless + ", slowMo: " + slowMo + "ms)");
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null && browser.isConnected()) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        System.out.println("Playwright cerrado");
    }

    @BeforeEach
    void createContextAndPage() {
        if (port == 0) {
            throw new IllegalStateException("El puerto del servidor no está disponible. " +
                    "Verificar que @SpringBootTest esté configurado correctamente.");
        }

        baseUrl = "http://localhost:" + port;

        boolean videoEnabled = Boolean.parseBoolean(System.getProperty("playwright.video.enabled", "false"));
        int timeout = Integer.parseInt(System.getProperty("playwright.timeout", "30000"));

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setLocale("es-ES")
                .setTimezoneId("America/Santo_Domingo");

        if (videoEnabled) {
            contextOptions.setRecordVideoDir(Paths.get("target/videos/"));
        }

        context = browser.newContext(contextOptions);
        context.setDefaultTimeout(timeout);
        context.setDefaultNavigationTimeout(timeout);

        page = context.newPage();

        page.onConsoleMessage(msg -> {
            switch (msg.type()) {
                case "error" -> System.err.println(" Console Error: " + msg.text());
                case "warning" -> System.out.println(" Console Warning: " + msg.text());
                case "info" -> System.out.println(" Console Info: " + msg.text());
                default -> { /* Ignorar otros tipos */ }
            }
        });

        page.onPageError(error -> {
            System.err.println(" Page Error: " + error);
            takeScreenshot("page-error");
        });

        page.onResponse(response -> {
            if (response.status() >= 400) {
                System.err.println(" HTTP Error " + response.status() + ": " + response.url());
            }
        });

        page.onRequestFailed(request -> {
            System.err.println(" Request Failed: " + request.url() + " - " + request.failure());
        });

        System.out.println(" Nueva página creada para: " + baseUrl);
    }

    @AfterEach
    void closeContext() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
        System.out.println(" Contexto cerrado correctamente");
    }

    protected void loginWithKeycloak(String username, String password) {
        try {
            System.out.println(" Iniciando login para usuario: " + username);

            page.navigate(baseUrl + "/login");
            page.waitForSelector("h1", new Page.WaitForSelectorOptions().setTimeout(5000));

            takeScreenshot("before-keycloak-redirect");

            page.click("button[type='submit']");
            page.waitForSelector("#username", new Page.WaitForSelectorOptions().setTimeout(15000));

            page.fill("#username", username);
            page.fill("#password", password);

            takeScreenshot("keycloak-credentials-filled");

            page.click("#kc-login");
            page.waitForURL("**/dashboard", new Page.WaitForURLOptions().setTimeout(20000));

            System.out.println(" Login exitoso para: " + username);
            takeScreenshot("login-success");

        } catch (Exception e) {
            System.err.println(" Error en login: " + e.getMessage());
            takeScreenshot("login-error-" + username);
            throw new RuntimeException("Error durante el login para usuario: " + username, e);
        }
    }

    protected void logout() {
        try {
            System.out.println(" Cerrando sesión...");

            page.click(".dropdown-toggle.profile-pic");
            page.click(".logout-item");
            page.waitForURL("**/login**", new Page.WaitForURLOptions().setTimeout(10000));

            System.out.println(" Logout exitoso");
            takeScreenshot("logout-success");

        } catch (Exception e) {
            System.err.println(" Error en logout: " + e.getMessage());
            takeScreenshot("logout-error");
            throw new RuntimeException("Error durante el logout", e);
        }
    }

    protected void takeScreenshot(String name) {
        try {
            boolean screenshotsEnabled = Boolean.parseBoolean(
                    System.getProperty("playwright.screenshots.enabled", "true"));

            if (screenshotsEnabled) {
                Paths.get("target/screenshots").toFile().mkdirs();

                String timestamp = String.valueOf(System.currentTimeMillis());
                String filename = name + "-" + timestamp + ".png";

                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get("target/screenshots/" + filename))
                        .setFullPage(true));

                System.out.println(" Screenshot guardado: " + filename);
            }
        } catch (Exception e) {
            System.err.println(" Error tomando screenshot: " + e.getMessage());
        }
    }

    protected Locator waitForElement(String selector) {
        return page.locator(selector).first();
    }

    protected void assertUrlContains(String urlPart) {
        String currentUrl = page.url();
        Assertions.assertTrue(currentUrl.contains(urlPart),
                "Expected URL to contain: " + urlPart + ", but was: " + currentUrl);
    }

    protected void waitForElementVisible(String selector) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }

    protected void smoothScroll(String selector) {
        page.locator(selector).scrollIntoViewIfNeeded();
        page.waitForTimeout(500);
    }

    protected void navigateAndWait(String path) {
        String fullUrl = baseUrl + path;
        page.navigate(fullUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        System.out.println(" Navegado a: " + fullUrl);
    }

    protected void printDebugInfo() {
        System.out.println("---  DEBUG INFO ---");
        System.out.println("URL actual: " + page.url());
        System.out.println("Título: " + page.title());
        System.out.println("Puerto del servidor: " + port);
        System.out.println("Base URL: " + baseUrl);
        System.out.println("-------------------");
    }

    protected boolean elementExists(String selector) {
        try {
            return page.locator(selector).count() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    protected void safeClick(String selector) {
        waitForElementVisible(selector);
        page.locator(selector).click();
    }

    protected void safeFill(String selector, String text) {
        waitForElementVisible(selector);
        page.locator(selector).fill(text);
    }
}