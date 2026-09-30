package tests;

import api.client.BookApiClient;
import auth.AuthSession;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import ui.ProfilePage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteBookFromCollectionTest extends BaseTest {

    private final BookApiClient bookApiClient = new BookApiClient();

    @Feature("Управління колекцією книг")
    @Story("Видалення книг")
    @Test(description = "Авторизований користувач видаляє книгу з колекції")
    @Description("""
            Передумова: через API створюється юзер, генерується токен, в колекцію додається 1 книга.
            UI: профіль відкривається через cookie injection, книга видаляється через іконку Trash
            з підтвердженням в модальному вікні.
            Очікування: книга зникає з таблиці на UI, а масив books в GET /User стає порожнім.
            """)
    public void deleteBookFromCollection() {
        createTestUserViaApi();

        String isbn = BookApiClient.DEFAULT_ISBN;

        AuthSession.injectAuthCookies(testUserId, testUserName, testUserToken, testUserTokenExpires);
        bookApiClient.addBookToUser(testUserId, testUserToken, isbn);

        new ProfilePage()
                .open()
                .shouldBeOpened()
                .shouldBookVisible(isbn)
                .deleteBook(isbn)
                .shouldBookNotBeVisible(isbn);

        List<String> booksAfterDeletion = userApiClient.getUserBookIsbns(testUserId, testUserToken);

        assertThat(booksAfterDeletion)
                .as("Книга з ISBN %s має зникнути з колекції користувача після видалення через UI", isbn)
                .doesNotContain(isbn);

        assertThat(booksAfterDeletion)
                .as("Масив books користувача має бути порожнім — до видалення в колекції була лише 1 книга (ISBN %s)", isbn)
                .isEmpty();
    }
}