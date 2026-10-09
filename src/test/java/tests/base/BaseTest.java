package tests.base;

import api.client.UserApiClient;
import api.model.CreateUserResponse;
import api.model.GenerateTokenResponse;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import config.Config;
import config.LoggingConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.testng.AllureTestNg;
import io.restassured.RestAssured;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

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

    // Protects RestAssured.filters(...) from being added multiple times.
    private static final AtomicBoolean LOGGING_INITIALIZED = new AtomicBoolean(false);

    protected final UserApiClient userApiClient = new UserApiClient();

    // Test user data — populated only if a specific test
    // requires it (authorized scenario)
    protected String testUserId;
    protected String testUserName;
    protected String testUserPassword;
    protected String testUserToken;
    protected String testUserTokenExpires;

    /**
     * Single entry point for suite setup.
     * Executes once for EACH test class participating in the suite
     * (this is a TestNG feature for inherited @BeforeSuite methods)
     */
    @BeforeSuite(alwaysRun = true)
    public void suiteSetUp() {
        cleanAllureResultsDirectory();
        setupRestAssuredLogging();
    }

    /**
     * Cleans target/allure-results before the test run starts.
     * First call (for the first class in the suite)
     * deletes the directory; each subsequent call (for other classes) will
     * immediately see that the directory is already gone and exit via return —
     * without side effects and without error.
     */
    private void cleanAllureResultsDirectory() {
        if (!Files.exists(ALLURE_RESULTS_DIR)) {
            return; // nothing to clean — either first run or already cleaned by previous class
        }
        try (Stream<Path> paths = Files.walk(ALLURE_RESULTS_DIR)) {
            paths.sorted(Comparator.reverseOrder()) // files first, then parent directories
                    .forEach(this::deleteQuietly);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to clean Allure results directory: " + ALLURE_RESULTS_DIR, e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            // Not critical — file might be locked by antivirus or still open,
            // don't fail the entire test run because of this
            System.err.println("Failed to delete: " + path + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Attaches global REST Assured filters: request/response logging
     * via Log4j2 and attaching them to Allure report.
     * Guard (LOGGING_INITIALIZED) is mandatory: RestAssured.filters(...)
     * ADDS filters to a static list, not replaces it. Without the guard,
     * each test class in the suite would add its own copy of filters —
     * and each request would be logged and attached to Allure N times,
     * where N is the number of classes in testng.xml.
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
        Configuration.timeout = Config.uiTimeoutMs();
        Configuration.pollingInterval = Config.uiPollingIntervalMs();
        Configuration.pageLoadTimeout = Config.pageLoadTimeoutMs();

        // CI runner has no graphical environment — headless is mandatory.
        // Locally, for debugging convenience, we keep normal mode with visible browser.
        Configuration.headless = System.getenv("CI") != null;

        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide().screenshots(true).savePageSource(false));
    }

    /**
     * Called explicitly from a test that needs a pre-created
     * authorized user. User is created via API, login
     * in the test itself is still performed via UI.
     */
    protected void createTestUserViaApi() {
        testUserName = "qa_user_" + UUID.randomUUID().toString().substring(0, 8);
        testUserPassword = "StrongPass123!";

        CreateUserResponse response = userApiClient.createUser(testUserName, testUserPassword);
        testUserId = response.userID();
        GenerateTokenResponse tokenResponse = userApiClient.generateTokenDetails(testUserName, testUserPassword);
        testUserToken = tokenResponse.token();
        testUserTokenExpires = tokenResponse.expires();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // Post-condition: remove test user if one was created
        try {
            if (testUserId != null) {
                userApiClient.deleteUser(testUserId, testUserToken);
            }
        } finally {
            closeWebDriver();
        }
    }
}


