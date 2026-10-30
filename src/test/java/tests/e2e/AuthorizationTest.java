package tests.e2e;

import auth.AuthSession;
import data.TestUser;
import data.UserFactory;
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
            groups = {"smoke", "regression", "e2e"}
    )
    @Description("""
            Precondition: a new test user is created via API, user is authorized
            (session is established). UI: Book Store page is opened.
            Verification: Login button is not visible on the page, user name on the page
            matches the one used for login.
            """)
    public void authorizedUserSeesUsernameAndProfileIcon() {

        createTestUserViaApi();
        TestUser user = testUser();

        ProfilePage profilePage = new LoginPage()
                .open()
                .loginAs(user.name(), user.password());

        profilePage
                .shouldBeOpened()
                .shouldSeeUserName(user.name())
                .shouldSeeLogoutButton();

        new BookStorePage()
                .open()
                .shouldBeOpened()
                .shouldSeeUserName(user.name())
                .shouldNotSeeLoginButton();

        String sessionToken = AuthSession.getAuthTokenFromCookie();
        assertThat(sessionToken)
                .as("After successful login, auth token should appear in browser cookie")
                .isNotBlank();

        // The UI login issues a new token, and demoqa may invalidate the previous one.
        // Cleanup in tearDown must use the token that is valid now.
        updateTestUserToken(sessionToken, user.tokenExpires());
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
        TestUser user = testUser();

        new LoginPage()
                .open()
                .attemptLoginAs(user.name(), UserFactory.WRONG_PASSWORD)
                .shouldSeeInvalidCredentialsError()
                .verifyIsStillOnLoginPage();
    }

    @Feature("Authentication")
    @Story("Login")
    @Test(
            description = "User cannot login with empty password",
            groups = {"regression", "ui"}
    )
    @Description("""
            Precondition: a new test user is created via API, user is not authorized.
            UI: attempt to authorize with empty password is made.
            Verification: The password input is highlighted with a red border.
            User remains on login page.
            """)
    public void userCannotLoginWithEmptyPassword() {
        createTestUserViaApi();
        TestUser user = testUser();

        new LoginPage()
                .open()
                .attemptLoginAs(user.name(), "")
                .shouldSeeInvalidPasswordInput()
                .verifyIsStillOnLoginPage();
    }

    @Feature("Authentication")
    @Story("Logout")
    @Test(
            description = "Authorized user can successfully log out",
            groups = {"smoke", "regression", "ui"}
    )
    @Description("""
            Precondition: A new test user is created via API, user is authorized.
            UI: User clicks the Logout button on the Profile page.
            Verification: User is redirected to the Login page, check that the 'token'
            cookie is cleared/empty. Re-open Profile page directly via URL. Verify that
            application shows unauthenticated state (warning message with Login and Register links).
            Verify that the 'token' cookie remains empty and session was not restored.
            """)
    public void authorizedUserCanLogout() {
        createTestUserViaApi();
        TestUser user = testUser();

        AuthSession.injectAuthCookies(user.id(), user.name(), user.token(), user.tokenExpires());
        new ProfilePage()
                .open()
                .shouldBeOpened()
                .shouldSeeUserName(user.name())
                .shouldSeeLogoutButton()
                .clickLogoutButton()
                .shouldBeOpened()
                .shouldSeeLoginButton();

        assertThat(AuthSession.getAuthTokenFromCookie())
                .as("After logout, auth token cookie should be empty or deleted")
                .isBlank();

        // Try to access Profile again after logout
        new ProfilePage().open()
                .shouldBeOpened()
                .shouldSeeUnauthenticatedState();

        // Verify that the auth token cookie remains cleared
        assertThat(AuthSession.getAuthTokenFromCookie())
                .as("Accessing Profile after logout must not restore the auth token")
                .isBlank();
    }
}
