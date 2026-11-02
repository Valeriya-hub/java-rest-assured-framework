package config;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;

public final class BrowserConfigurator {
    private BrowserConfigurator() {
    }

    public static void apply() {
        Configuration.pageLoadStrategy = Config.pageLoadStrategy();

        if (Config.noSandbox()) {
            switch (Config.browser()) {
                case "chrome" -> Configuration.browserCapabilities =
                        new ChromeOptions().addArguments("--no-sandbox");
                case "edge" -> Configuration.browserCapabilities =
                        new EdgeOptions().addArguments("--no-sandbox");
                default -> { /* Firefox has no such flag */ }
            }
        }
    }
}
