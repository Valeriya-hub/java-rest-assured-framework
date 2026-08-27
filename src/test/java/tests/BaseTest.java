package tests;

import api.client.UserApiClient;
import api.model.CreateUserResponse;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import config.Config;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.closeWebDriver;

@Listeners({AllureTestNg.class, listener.AllureAttachmentListener.class})
public abstract class BaseTest {

    private static final Path ALLURE_RESULTS_DIR = Paths.get("target", "allure-results");

    protected final UserApiClient userApiClient = new UserApiClient();

    // Дані тестового юзера — заповнюються тільки якщо конкретний тест
    // цього потребує (авторизований сценарій)
    protected String testUserId;
    protected String testUserName;
    protected String testUserPassword;
    protected String testUserToken;

    /**
     * Очищує target/allure-results ПЕРЕД стартом усього сьюту.
     * Виконується один раз, незалежно від кількості тестових класів у сьюті —
     * гарантія TestNG для @BeforeSuite методів.
     * Дозволяє запускати `mvn test` (без clean) або тести напряму з IDE
     * без накопичення застарілих результатів попередніх прогонів.
     */
    @BeforeSuite(alwaysRun = true)
    public void cleanAllureResultsDirectory() {
        if (!Files.exists(ALLURE_RESULTS_DIR)) {
            return; // нічого чистити — це перший запуск
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


