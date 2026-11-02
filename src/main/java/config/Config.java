package config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public final class Config {
    private static final String ENV = System.getProperty("env", "qa");
    private static final Properties PROPERTIES = new Properties();
    private static final Set<String> SUPPORTED_BROWSERS = Set.of("chrome", "firefox", "edge");
    private static final Set<String> PAGE_LOAD_STRATEGIES = Set.of("normal", "eager", "none");

    static {
        String resourceName = "config-" + ENV + ".properties";
        try (InputStream is = Config.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalStateException(
                        "File " + resourceName + " not found in classpath (src/test/resources). "
                                + "Check -Denv value (current: '" + ENV + "').");
            }
            PROPERTIES.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + resourceName, e);
        }
    }

    private Config() {
    }

    /** Global timeout for waiting for elements (ms). Can be overridden with: -Dui.timeout.ms=10000 */
    public static long uiTimeoutMs() {
        return getLong("ui.timeout.ms");
    }

    /** Selenide polling interval (ms). */
    public static long uiPollingIntervalMs() {
        return getLong("ui.polling.ms");
    }

    /** Page load timeout (ms). */
    public static long pageLoadTimeoutMs() {
        return getLong("ui.pageload.timeout.ms");
    }

    /** Only for genuinely long-running operations (file generation, heavy async processes). */
    public static Duration longOperationTimeout() {
        return Duration.ofSeconds(getLong("long.operation.timeout.sec"));
    }

    public static String uiBaseUrl() {
        return get("ui.base.url");
    }

    public static String apiBaseUrl() {
        return get("api.base.url");
    }

    public static String bookStoreUrl() {
        return uiBaseUrl() + get("ui.path.books");
    }

    public static String loginUrl() {
        return uiBaseUrl() + get("ui.path.login");
    }

    public static String profileUrl() {
        return uiBaseUrl() + get("ui.path.profile");
    }

    public static String bookSearchUrl(String searchQuery) {
        return uiBaseUrl() + get("ui.path.books") + "?search=" + searchQuery;
    }

    private static String get(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Property '" + key + "' is not set in config-" + ENV + ".properties, or via -D" + key);
        }
        return value;
    }

    private static long getLong(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Property '" + key + "' is not set in config-" + ENV + ".properties, or via -D" + key);
        }
        return Long.parseLong(value);
    }

    public static String envName() {
        return ENV;
    }

    public static String browser() {
        String value = System.getProperty("browser", PROPERTIES.getProperty("browser", "chrome"))
                .trim()
                .toLowerCase(Locale.ROOT);
        if (!SUPPORTED_BROWSERS.contains(value)) {
            throw new IllegalStateException(
                    "Unsupported browser '" + value + "'. Supported: " + SUPPORTED_BROWSERS);
        }
        return value;
    }

    public static String browserSize() {
        return System.getProperty("browser.size", PROPERTIES.getProperty("browser.size", "1920x1080"));
    }

    /**
     * Determines the headless mode configuration following the precedence hierarchy:
     * 1. Command-line system property (-Dheadless=true/false) — highest priority for local debugging.
     * 2. CI environment variable (GitHub Actions runtime) — ensures automatic headless execution in pipelines.
     * 3. Fallback to properties file configuration (headless.enabled).
     */
    public static boolean isHeadless() {
        String headlessProp = System.getProperty("headless");
        if (headlessProp != null) {
            return Boolean.parseBoolean(headlessProp);
        }
        if (System.getenv("CI") != null) {
            return true;
        }
        return Boolean.parseBoolean(PROPERTIES.getProperty("headless.enabled", "false"));
    }

    /**
     * Page load strategy. "eager" returns control at DOMContentLoaded and does not wait for
     * ads, trackers and fonts; Selenide then waits for the elements the test actually needs.
     */
    public static String pageLoadStrategy() {
        String value = System.getProperty("ui.pageload.strategy",
                        PROPERTIES.getProperty("ui.pageload.strategy", "eager"))
                .trim().toLowerCase(Locale.ROOT);
        if (!PAGE_LOAD_STRATEGIES.contains(value)) {
            throw new IllegalStateException(
                    "Unsupported page load strategy '" + value + "'. Supported: " + PAGE_LOAD_STRATEGIES);
        }
        return value;
    }

    /** Disables the Chromium sandbox (Chrome/Edge). Off by default, a CI-only fallback: -Dbrowser.no.sandbox=true */
    public static boolean noSandbox() {
        return Boolean.parseBoolean(System.getProperty("browser.no.sandbox",
                PROPERTIES.getProperty("browser.no.sandbox", "false")));
    }
}
