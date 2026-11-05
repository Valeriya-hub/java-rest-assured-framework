package listener;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TestNG listener that attaches to Allure report when a UI test fails:
 * - screenshot of the page at the moment of failure;
 * - HTML page source;
 * - current URL;
 * - browser console logs (if driver supports them);
 * - names of cookies present in the browser (shows whether the login reached the point of setting the token);
 * - network log of the demoqa Account API calls (Chromium only, needs the performance log capability).
 * <p>
 * Complements io.qameta.allure.selenide.AllureSelenide (which logs each
 * Selenide step), not duplicates it — here the state is captured EXACTLY at the
 * moment of test failure, including cases when the test failed on assert
 * AFTER all Selenide actions have already executed successfully.
 */
public class AllureAttachmentListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(AllureAttachmentListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Only requests to this API are written to the network attachment, everything else is noise
    private static final String AUTH_API_PATH = "/Account/v1/";

    @Override
    public void onTestFailure(ITestResult result) {
        if (WebDriverRunner.hasWebDriverStarted()) {
            try {
                attachScreenshot();
                attachPageSource();
                attachCurrentUrl();
                attachBrowserConsoleLogs();
                attachCookieNames();
                attachAuthNetworkLog();
            } catch (Exception e) {
                log.error("Failed to attach diagnostic data for test '{}': {}",
                        result.getName(), e.getMessage(), e);
            }
        }
    }

    @Attachment(value = "Screenshot at failure", type = "image/png")
    private byte[] attachScreenshot() {
        return ((TakesScreenshot) WebDriverRunner.getWebDriver())
                .getScreenshotAs(OutputType.BYTES);
    }

    @Attachment(value = "HTML page at failure", type = "text/html")
    private String attachPageSource() {
        return WebDriverRunner.getWebDriver().getPageSource();
    }

    @Attachment(value = "URL at failure", type = "text/plain")
    private String attachCurrentUrl() {
        return Selenide.webdriver().driver().url();
    }

    @Attachment(value = "Browser console logs", type = "text/plain")
    private String attachBrowserConsoleLogs() {
        try {
            StringBuilder logs = new StringBuilder();
            WebDriverRunner.getWebDriver()
                    .manage()
                    .logs()
                    .get(LogType.BROWSER)
                    .getAll()
                    .forEach((LogEntry entry) -> logs.append(entry).append(System.lineSeparator()));
            return logs.toString();
        } catch (Exception e) {
            // Not all browsers/drivers support BROWSER logs (e.g., Firefox without special settings)
            return "Console logs unavailable: " + e.getMessage();
        }
    }

    /**
     * Cookie NAMES only, never values: tokens must not end up in a public report.
     * If the "token" cookie is present but the URL is still /login, the login request succeeded
     * and the redirect failed. If it is absent, the login request did not finish successfully.
     */
    @Attachment(value = "Cookie names at failure", type = "text/plain")
    private String attachCookieNames() {
        try {
            String names = WebDriverRunner.getWebDriver().manage().getCookies().stream()
                    .map(Cookie::getName)
                    .sorted()
                    .collect(Collectors.joining(System.lineSeparator()));
            return names.isEmpty() ? "(no cookies)" : names;
        } catch (Exception e) {
            return "Cookies unavailable: " + e.getMessage();
        }
    }

    /**
     * Writes request / response status / failure of the Account API calls with timestamps,
     * so the difference between REQUEST and RESPONSE lines shows the server latency.
     * Works on Chromium (Chrome, Edge) only and only if the performance log is enabled
     * in the browser capabilities. Otherwise the attachment says that it is unavailable.
     */
    @Attachment(value = "Network log: Account API (Chromium only)", type = "text/plain")
    private String attachAuthNetworkLog() {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            Map<String, String> urlByRequestId = new HashMap<>();
            StringBuilder out = new StringBuilder();

            for (LogEntry entry : driver.manage().logs().get(LogType.PERFORMANCE).getAll()) {
                JsonNode message = MAPPER.readTree(entry.getMessage()).path("message");
                JsonNode params = message.path("params");
                String requestId = params.path("requestId").asText();
                String time = Instant.ofEpochMilli(entry.getTimestamp()).toString();

                switch (message.path("method").asText()) {
                    case "Network.requestWillBeSent" -> {
                        String url = params.path("request").path("url").asText();
                        if (url.contains(AUTH_API_PATH)) {
                            urlByRequestId.put(requestId, url);
                            out.append(time).append(" REQUEST  ")
                                    .append(params.path("request").path("method").asText())
                                    .append(' ').append(url).append(System.lineSeparator());
                        }
                    }
                    case "Network.responseReceived" -> {
                        String url = urlByRequestId.get(requestId);
                        if (url != null) {
                            out.append(time).append(" RESPONSE ")
                                    .append(params.path("response").path("status").asInt())
                                    .append(' ').append(url).append(System.lineSeparator());
                        }
                    }
                    case "Network.loadingFailed" -> {
                        String url = urlByRequestId.get(requestId);
                        if (url != null) {
                            out.append(time).append(" FAILED   ")
                                    .append(params.path("errorText").asText())
                                    .append(' ').append(url).append(System.lineSeparator());
                        }
                    }
                    default -> {
                        // Other network events are not relevant for the diagnosis
                    }
                }
            }
            return out.isEmpty() ? "No Account API calls found in the performance log" : out.toString();
        } catch (Exception e) {
            // Firefox has no performance log; Chromium has none if the capability is not enabled
            return "Network log unavailable: " + e.getMessage();
        }
    }
}