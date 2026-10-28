package tests.base;

import api.client.UserApiClient;
import api.model.CredentialsRequest;
import api.model.CredentialsResponse;
import api.model.GenerateTokenResponse;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import config.Config;
import config.LoggingConfig;
import data.TestUser;
import data.UserFactory;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.closeWebDriver;

@Listeners({AllureTestNg.class, listener.AllureAttachmentListener.class})
public abstract class BaseTest {

    private static final Path ALLURE_RESULTS_DIR = Paths.get("target", "allure-results");

    // Guards RestAssured.filters(...) against being added more than once
    private static final AtomicBoolean LOGGING_INITIALIZED = new AtomicBoolean(false);

    /**
     * The user of the test running on the current thread.
     *
     * TestNG uses ONE instance of a test class for all its methods, so instance fields
     * would be shared by tests that run in parallel. A ThreadLocal gives every running
     * test its own user. A thread runs one test at a time, and @BeforeMethod/@AfterMethod
     * run on the same thread as the test method.
     */
    private static final ThreadLocal<TestUser> CURRENT_USER = new ThreadLocal<>();

    protected final UserApiClient userApiClient = new UserApiClient();

    /**
     * Single entry point of the suite setup. TestNG runs it before any test of the suite starts.
     */
    @BeforeSuite(alwaysRun = true)
    public void suiteSetUp() {
        cleanAllureResultsDirectory();
        setupRestAssuredLogging();
    }

    /**
     * Cleans target/allure-results before the run. If the directory does not exist
     *  (first run, or already cleaned by another invocation), it simply returns.
     */
    private void cleanAllureResultsDirectory() {
        if (!Files.exists(ALLURE_RESULTS_DIR)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(ALLURE_RESULTS_DIR)) {
            paths.sorted(Comparator.reverseOrder()) // files first, then parent directories
                    .forEach(this::deleteQuietly);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to clean the Allure results directory: " + ALLURE_RESULTS_DIR, e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            // Not critical: the file may be locked by an antivirus or still open.
            // A whole test run should not fail because of this.
            System.err.println("Failed to delete: " + path + " (" + e.getMessage() + ")");
        }
    }

    /**
     * Registers global REST Assured filters: request/response logging via Log4j2
     * and attaching them to the Allure report.
     *
     * The guard is required: RestAssured.filters(...) ADDS filters to a static list
     * instead of replacing it. Without the guard every test class would add its own copy
     * and each request would be logged and attached N times.
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

        // Priority:
        // 1. If `-Dheadless=true` or `-Dheadless=false` is passed via the Maven command line.
        // 2. If running on a CI server (GitHub Actions).
        // 3. Default for local execution — false (the browser opens visually).
        String headlessProp = System.getProperty("headless");
        if (headlessProp != null) {
            Configuration.headless = Boolean.parseBoolean(headlessProp);
        } else {
            Configuration.headless = System.getenv("CI") != null;
        }

        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide().screenshots(true).savePageSource(false));
    }

    /**
     * Returns the user of the current test.
     * Fails with a clear message if the test did not create one.
     */
    protected TestUser testUser() {
        TestUser user = CURRENT_USER.get();
        if (user == null) {
            throw new IllegalStateException(
                    "No test user for this test: call createTestUserViaApi() "
                            + "or registerCreatedUserForCleanup() first");
        }
        return user;
    }

    protected void setTestUser(TestUser user) {
        CURRENT_USER.set(user);
    }

    /**
     * Replaces the token of the current test user, e.g. after a new token was generated
     * (demoqa may invalidate the previous one), so that cleanup uses a valid token.
     */
    protected void updateTestUserToken(String token, String tokenExpires) {
        setTestUser(testUser().withToken(token, tokenExpires));
    }

    /**
     * Called explicitly by a test that needs a pre-created user.
     * The user is created via API; the login itself is still done through the UI in the test.

     */
    protected void createTestUserViaApi() {
        CredentialsRequest credentials = UserFactory.randomCredentials();

        CredentialsResponse created = userApiClient.createUser(credentials.userName(), credentials.password());
        // Register right after creation, so the user is cleaned up even if token generation fails.
        setTestUser(new TestUser(created.userID(), credentials.userName(), credentials.password(), null, null));

        GenerateTokenResponse tokenResponse =
                userApiClient.generateTokenDetails(credentials.userName(), credentials.password());
        updateTestUserToken(tokenResponse.token(), tokenResponse.expires());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // Post-condition: remove the test user if the test created one.
        try {
            TestUser user = CURRENT_USER.get();
            if (user != null) {
                userApiClient.deleteUser(user.id(), user.token());
            }
        } finally {
            // TestNG reuses threads, so the value must be removed, otherwise the next
            // test on this thread would inherit this user.
            CURRENT_USER.remove();
            closeWebDriver();
        }
    }
}


