package tests;

import api.client.UserApiClient;
import api.model.CreateUserResponse;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import config.Config;
import config.LoggingConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.RestAssured;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.closeWebDriver;

@Listeners({AllureTestNg.class, listener.AllureAttachmentListener.class})
public abstract class BaseTest {

    private static final Path ALLURE_RESULTS_DIR = Paths.get("target", "allure-results");

    // Захищає RestAssured.filters(...) від повторного додавання.
    private static final AtomicBoolean LOGGING_INITIALIZED = new AtomicBoolean(false);

    protected final UserApiClient userApiClient = new UserApiClient();

    // Дані тестового юзера — заповнюються тільки якщо конкретний тест
    // цього потребує (авторизований сценарій)
    protected String testUserId;
    protected String testUserName;
    protected String testUserPassword;
    protected String testUserToken;

    /**
     * Єдина точка входу для сетапу всього сьюту.
     *
     * Виконується один раз на КОЖЕН тестовий клас, що бере участь у suite
     * (це особливість TestNG для успадкованих @BeforeSuite методів)
     */
    @BeforeSuite(alwaysRun = true)
    public void suiteSetUp() {
        cleanAllureResultsDirectory();
        setupRestAssuredLogging();
    }

    /**
     * Очищує target/allure-results перед стартом прогону.
     *
     * Перший виклик (для першого класу в suite)
     * видаляє директорію; кожен наступний виклик (для інших класів) одразу
     * побачить, що директорії вже немає, і вийде через return —
     * без побічних ефектів і без помилки.
     */
    private void cleanAllureResultsDirectory() {
        if (!Files.exists(ALLURE_RESULTS_DIR)) {
            return; // нічого чистити — або перший запуск, або вже очищено попереднім класом
        }
        try (Stream<Path> paths = Files.walk(ALLURE_RESULTS_DIR)) {
            paths.sorted(Comparator.reverseOrder()) // спочатку файли, потім батьківські директорії
                    .forEach(this::deleteQuietly);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не вдалося очистити директорію Allure-результатів: " + ALLURE_RESULTS_DIR, e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            // Не критично — файл міг бути заблокований антивірусом чи ще відкритий,
            // не варто валити весь прогін тестів через це
            System.err.println("Не вдалося видалити: " + path + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Підключає глобальні фільтри REST Assured: логування запитів/відповідей
     * через Log4j2 і прикріплення їх до Allure-звіту.
     *
     * Guard (LOGGING_INITIALIZED) обов'язковий: RestAssured.filters(...)
     * ДОДАЄ фільтри до статичного списку, а не замінює його. Без guard'а
     * кожен тестовий клас у suite додав би свою копію фільтрів —
     * і кожен запит логувався б і прикріплювався до Allure N разів,
     * де N — кількість класів у testng.xml.
     */
    private void setupRestAssuredLogging() {
        if (LOGGING_INITIALIZED.compareAndSet(false, true)) {
            RestAssured.filters(
                    LoggingConfig.requestFilter(),
                    LoggingConfig.responseFilter(),
                    new AllureRestAssured()
            );
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        Configuration.baseUrl = Config.uiBaseUrl();
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 8000;

        // На CI-runner немає графічного середовища — headless обов'язковий.
        // Локально, для зручності дебагу, лишаємо звичайний режим з видимим браузером.
        Configuration.headless = System.getenv("CI") != null;

        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide().screenshots(true).savePageSource(false));
    }

    /**
     * Викликається явно з тесту, якому потрібен заздалегідь створений
     * авторизований користувач. Юзер створюється через API, логін
     * в самому тесті все одно виконується через UI.
     */
    protected void createTestUserViaApi() {
        testUserName = "qa_user_" + UUID.randomUUID().toString().substring(0, 8);
        testUserPassword = "StrongPass123!";

        CreateUserResponse response = userApiClient.createUser(testUserName, testUserPassword);
        testUserId = response.getUserID();
        testUserToken = userApiClient.generateToken(testUserName, testUserPassword);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // Post-condition: приберемо тестового юзера, якщо він створювався
        userApiClient.deleteUser(testUserId, testUserToken);

        closeWebDriver();
    }

}


