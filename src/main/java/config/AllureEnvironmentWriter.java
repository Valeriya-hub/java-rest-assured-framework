package config;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * TestNG listener responsible for dynamically generating the environment.properties
 * file inside the Allure results directory.
 * <p>
 * This configuration enables Allure Report to populate the Environment widget on the Overview dashboard,
 * providing clear metadata (such as target browser, execution mode, base URL, and environment name)
 * for each automated test run in CI/CD pipelines.
 */
public class AllureEnvironmentWriter {

    private AllureEnvironmentWriter() {
    }

    public static void write(Path resultsDir) {
        Properties env = new Properties();
        env.setProperty("Browser", Config.browser());
        env.setProperty("Headless", String.valueOf(Config.isHeadless()));
        env.setProperty("Browser.Size", Config.browserSize());
        env.setProperty("Environment", Config.envName());
        env.setProperty("UI.Base.URL", Config.uiBaseUrl());
        env.setProperty("API.Base.URL", Config.apiBaseUrl());
        env.setProperty("OS", System.getProperty("os.name"));
        env.setProperty("Java", System.getProperty("java.version"));

        try {
            Files.createDirectories(resultsDir);
            try (OutputStream out = Files.newOutputStream(resultsDir.resolve("environment.properties"))) {
                env.store(out, null);
            }
        } catch (IOException e) {
            // Not critical: the report just won't have the Environment widget.
            System.err.println("Failed to write Allure environment.properties: " + e.getMessage());
        }
    }
}