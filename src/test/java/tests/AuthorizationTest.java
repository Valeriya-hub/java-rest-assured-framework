package tests;

import auth.AuthSession;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import ui.BookStorePage;
import ui.LoginPage;
import ui.ProfilePage;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorizationTest extends BaseTest {

    @Feature("Authentication")
    @Story("Login")
    @Test(
            description = "Authorized user sees their name and profile icon",
            groups = {"smoke", "ui"}
    )
    @Description("""
            Precondition: a new test user is created via API, user is authorized
            (session is established). UI: Book Store page is opened.
            Verification: Login button is not visible on the page, user name on the page
            matches the one used for login.
            """)
    public void authorizedUserSeesUsernameAndProfileIcon() {

        createTestUserViaApi();

        ProfilePage profilePage = new LoginPage()
                .open()
                .loginAs(testUserName, testUserPassword);

        profilePage
                .shouldBeOpened()
                .shouldSeeUserName(testUserName)
                .shouldSeeLogoutButton();

        new BookStorePage()
                .open()
                .shouldBeOpened()
                .shouldSeeUserName(testUserName)
                .shouldNotSeeLoginButton();

        assertThat(AuthSession.getAuthTokenFromCookie())
                .as("After successful login, auth token should appear in browser cookie")
                .isNotBlank();
    }

    @Feature("Authentication")
    @Story("Login")
    @Test(
            description = "User cannot login with incorrect password",
            groups = {"regression", "ui"}
    )
    @Description("""
            Precondition: a new test user is created via API, user is not authorized.
            UI: attempt to authorize with incorrect password is made.
            Verification: incorrect password message appears on the page.
            User remains on login page.
            """)
    public void userCannotLoginWithWrongPassword() {
        createTestUserViaApi();

        new LoginPage()
                .open()
                .attemptLoginAs(testUserName, "WrongPassword123!")
                .shouldSeeInvalidCredentialsError()
                .verifyIsStillOnLoginPage();
    }
}
