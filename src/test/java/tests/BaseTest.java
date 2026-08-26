package tests;

import api.client.UserApiClient;
import api.model.CreateUserResponse;
import com.codeborne.selenide.Configuration;
import config.Config;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

import java.util.UUID;

import static com.codeborne.selenide.Selenide.closeWebDriver;

@Listeners({AllureTestNg.class, listener.AllureAttachmentListener.class})
public abstract class BaseTest {

    protected final UserApiClient userApiClient = new UserApiClient();

    // Дані тестового юзера — заповнюються тільки якщо конкретний тест
    // цього потребує (авторизований сценарій)
    protected String testUserId;
    protected String testUserName;
    protected String testUserPassword;
    protected String testUserToken;

    @BeforeTest
    static void setup() {
        Configuration.baseUrl = Config.uiBaseUrl();
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 8000;
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


