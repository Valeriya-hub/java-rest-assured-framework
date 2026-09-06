package tests;

import org.testng.annotations.Test;
import ui.BookStorePage;
import ui.LoginPage;
import ui.ProfilePage;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorizationTest extends BaseTest {

    @Test(description = "Авторизований користувач бачить своє ім'я та іконку профілю")
    public void authorizedUserSeesUsernameAndProfileIcon() {
        // Pre-condition: створюємо юзера через API,
        // сам логін виконується через UI — тест не порушує "тільки UI"
        createTestUserViaApi();

        ProfilePage profilePage = new LoginPage()
                .open()
                .loginAs(testUserName, testUserPassword);

        assertThat(profilePage.getDisplayedUserName())
                .as("Логін має завершитись успішно перед переходом на Book Store")
                .isEqualTo(testUserName);

        BookStorePage bookStorePage = new BookStorePage().open();

        assertThat(bookStorePage.isProfileIconVisible())
                .as("Іконка профілю має бути видима після успішного логіну")
                .isTrue();

        assertThat(bookStorePage.getDisplayedUserName())
                .as("Відображене ім'я користувача має збігатись з тим, під яким логінились")
                .isEqualTo(testUserName);
    }

    @Test(description = "Неавторизований користувач бачить кнопку Login замість профілю")
    public void unauthorizedUserSeesLoginButton() {
        BookStorePage bookStorePage = new BookStorePage().open();

        assertThat(bookStorePage.isLoginButtonVisible())
                .as("Кнопка Login має бути видима для неавторизованого користувача")
                .isTrue();

    }

    @Test(description = "Неавторизований користувач не бачить чекбоксів для додавання книг у колекцію")
    public void unauthorizedUserDoesNotSeeCollectionCheckboxes() {
        BookStorePage bookStorePage = new BookStorePage().open();

        assertThat(bookStorePage.areCollectionCheckboxesPresent())
                .as("Чекбокси додавання в колекцію не мають бути доступні без авторизації")
                .isFalse();
    }
}
