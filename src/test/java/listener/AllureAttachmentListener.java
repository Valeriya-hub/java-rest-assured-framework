package listener;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG-listener, який при падінні UI-тесту прикріплює до Allure-звіту:
 * - скріншот сторінки в момент фейлу;
 * - HTML page source;
 * - поточний URL;
 * - логи браузерної консолі (якщо драйвер їх підтримує).
 * <p>
 * Доповнює io.qameta.allure.selenide.AllureSelenide (який логує кожен
 * Selenide-крок), а не дублює його — тут фіксується стан САМЕ на момент
 * фейлу тесту, включно з випадками, коли тест впав на assert
 * ПІСЛЯ того, як усі Selenide-дії вже відпрацювали успішно.
 */
public class AllureAttachmentListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        if (WebDriverRunner.hasWebDriverStarted()) {
            attachScreenshot();
            attachPageSource();
            attachCurrentUrl();
            attachBrowserConsoleLogs();
        }
    }

    @Attachment(value = "Скріншот у момент падіння", type = "image/png")
    private byte[] attachScreenshot() {
        return ((TakesScreenshot) WebDriverRunner.getWebDriver())
                .getScreenshotAs(OutputType.BYTES);
    }

    @Attachment(value = "HTML сторінки в момент падіння", type = "text/html")
    private String attachPageSource() {
        return WebDriverRunner.getWebDriver().getPageSource();
    }

    @Attachment(value = "URL в момент падіння", type = "text/plain")
    private String attachCurrentUrl() {
        return Selenide.webdriver().driver().url();
    }

    @Attachment(value = "Логи консолі браузера", type = "text/plain")
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
            // Не всі браузери/драйвери підтримують BROWSER logs (напр. Firefox без спец. налаштувань)
            return "Логи консолі недоступні: " + e.getMessage();
        }
    }
}