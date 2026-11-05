package config;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.util.logging.Level;

public final class BrowserConfigurator {

    // chromedriver reads the logging preferences from "goog:loggingPrefs",
    // msedgedriver ignores it and reads them from "ms:loggingPrefs" instead.
    private static final String CHROME_LOGGING_PREFS = "goog:loggingPrefs";
    private static final String EDGE_LOGGING_PREFS = "ms:loggingPrefs";

    private BrowserConfigurator() {
    }

    public static void apply() {
        Configuration.pageLoadStrategy = Config.pageLoadStrategy();

        switch (Config.browser()) {
            case "chrome" -> Configuration.browserCapabilities = chromeOptions();
            case "edge" -> Configuration.browserCapabilities = edgeOptions();
            default -> { /* Firefox has no performance log and no sandbox flag: defaults are used */ }
        }
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.setCapability(CHROME_LOGGING_PREFS, loggingPreferences());
        if (Config.noSandbox()) {
            options.addArguments("--no-sandbox");
        }
        return options;
    }

    private static EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.setCapability(EDGE_LOGGING_PREFS, loggingPreferences());
        if (Config.noSandbox()) {
            options.addArguments("--no-sandbox");
        }
        return options;
    }

    /**
     * Enables the logs used by AllureAttachmentListener on test failure:
     * BROWSER - console messages, PERFORMANCE - network events (requests, statuses, failures).
     */
    private static LoggingPreferences loggingPreferences() {
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        return logPrefs;
    }
}