package config;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.util.List;
import java.util.logging.Level;

public final class BrowserConfigurator {

    // chromedriver reads the logging preferences from "goog:loggingPrefs",
    // msedgedriver ignores it and reads them from "ms:loggingPrefs" instead.
    private static final String CHROME_LOGGING_PREFS = "goog:loggingPrefs";
    private static final String EDGE_LOGGING_PREFS = "ms:loggingPrefs";

    /**
     * Flags that make a Chromium browser start faster and more predictably on a CI runner:
     * no first-run tasks, no background network calls (sync, updates), no extensions,
     * and no use of the small shared memory (/dev/shm).
     */
    private static final List<String> STABILITY_ARGUMENTS = List.of(
            "--disable-dev-shm-usage",
            "--no-first-run",
            "--no-default-browser-check",
            "--disable-extensions",
            "--disable-background-networking",
            "--disable-gpu"
    );

    private BrowserConfigurator() {
    }

    public static void apply() {
        Configuration.pageLoadStrategy = Config.pageLoadStrategy();

        switch (Config.browser()) {
            case "chrome" -> Configuration.browserCapabilities = chromeOptions();
            case "edge" -> Configuration.browserCapabilities = edgeOptions();
            default -> { /* Firefox has no performance log and no Chromium flags: defaults are used */ }
        }
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.setCapability(CHROME_LOGGING_PREFS, loggingPreferences());
        options.addArguments(STABILITY_ARGUMENTS);
        if (Config.noSandbox()) {
            options.addArguments("--no-sandbox");
        }
        return options;
    }

    private static EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.setCapability(EDGE_LOGGING_PREFS, loggingPreferences());
        options.addArguments(STABILITY_ARGUMENTS);
        if (Config.noSandbox()) {
            options.addArguments("--no-sandbox");
        }
        return options;
    }

    /**
     * Enables the logs used by AllureAttachmentListener on test failure:
     * BROWSER - console messages, PERFORMANCE - network events (requests, statuses, failures).
     * <p>
     * The performance log adds some overhead, so it can be switched off for a diagnostic run
     * with -Dbrowser.network.log=false (for example, to check that it is not the cause of a slow start).
     */
    private static LoggingPreferences loggingPreferences() {
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        if (isNetworkLogEnabled()) {
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        }
        return logPrefs;
    }

    private static boolean isNetworkLogEnabled() {
        return Boolean.parseBoolean(System.getProperty("browser.network.log", "true"));
    }
}