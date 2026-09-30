package tests;

import api.client.UserApiClient;
import auth.AuthSession;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import ui.BookPage;
import ui.BookStorePage;
import ui.ProfilePage;

import static org.assertj.core.api.Assertions.assertThat;

public class AddBookToCollectionTest extends BaseTest {

    @Feature("Управління колекцією книг")
    @Story("Додавання книг")
    @Test(description = "Користувач додає книгу в колекцію")
    @Description("""
            Передумова: через API створюється користувач, авторизаційні cookie підставляються в браузер.
            UI: відкривається Профіль -> перехід у Book Store -> вибір книги 'Git Pocket Guide' -> 
            натискання 'Add To Collection' та закриття alert.
            Перевірка: через API (GET /Account/v1/User) перевіряється, що ISBN доданої книги з'явився
            у колекції користувача.
            """)
    public void userAddBookToCollection() {
        createTestUserViaApi();

        AuthSession.injectAuthCookies(testUserId, testUserName, testUserToken, testUserTokenExpires);

        BookStorePage bookStorePage = new ProfilePage()
                .open()
                .shouldBeOpened()
                .clickGoToBookStoreButton();

        BookPage bookPage = bookStorePage
                .clickOnBook("Git Pocket Guide")
                .clickAddToCollectionButton();

        bookPage.dismissAlertIfPresent();
        String addedBookIsbn = bookPage.getIsbn();

        assertThat(userApiClient.getUserBookIsbns(testUserId, testUserToken))
                .as("Книга з ISBN %s має з'явитись в колекції користувача після додавання через UI", addedBookIsbn)
                .contains(addedBookIsbn);
    }
}
