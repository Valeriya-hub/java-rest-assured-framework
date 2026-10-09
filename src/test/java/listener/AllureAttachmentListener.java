package listener;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener that attaches to Allure report when a UI test fails:
 * - screenshot of the page at the moment of failure;
 * - HTML page source;
 * - current URL;
 * - browser console logs (if driver supports them).
 * <p>
 * Complements io.qameta.allure.selenide.AllureSelenide (which logs each
 * Selenide step), not duplicates it — here the state is captured EXACTLY at the
 * moment of test failure, including cases when the test failed on assert
 * AFTER all Selenide actions have already executed successfully.
 */
public class AllureAttachmentListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(AllureAttachmentListener.class);

    @Override
    public void onTestFailure(ITestResult result) {
        if (WebDriverRunner.hasWebDriverStarted()) {
            try {
            attachScreenshot();
            attachPageSource();
            attachCurrentUrl();
            attachBrowserConsoleLogs();
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
}