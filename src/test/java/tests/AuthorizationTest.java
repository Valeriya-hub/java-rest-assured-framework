package tests;

import auth.AuthSession;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import ui.BookStorePage;
import ui.LoginPage;
import ui.ProfilePage;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorizationTest extends BaseTest {
    private final String ERROR_MESSAGE = "Invalid username or password!";

    @Feature("Автентифікація")
    @Story("Логін")
    @Test(description = "Авторизований користувач бачить своє ім'я та іконку профілю")
    @Description("""
            Передумова: через API створюється новий тестовий користувач, користувач 
            авторизований (сесія встановлена). UI: відкривається сторінка Book Store.
            Перевірка: кнопка Login є не видимою на сторінці, ім'я користувача на сторінці
            таке саме з яким логінились.
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
                .as("Після успішного логіну auth-токен має з'явитись в cookie браузера")
                .isNotBlank();
    }

    @Feature("Автентифікація")
    @Story("Логін")
    @Test(description = "Користувач не може залогінитись з невірним паролем")
    @Description("""
            Передумова: через API створюється новий тестовий користувач, користувач не
            авторизований. UI: здійснюється спроба авторизації з невірним паролем.
            Перевірка: На сторінці з'являється повідомлення про не правильний пароль.
            Користувач залишається на сторінці логіну.
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
