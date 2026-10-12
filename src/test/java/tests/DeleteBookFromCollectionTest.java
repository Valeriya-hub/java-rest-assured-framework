package tests;

import api.client.BookApiClient;
import auth.AuthSession;
import data.Book;
import data.Books;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import ui.ProfilePage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteBookFromCollectionTest extends BaseTest {

    private final BookApiClient bookApiClient = new BookApiClient();

    @Feature("Book collection management")
    @Story("Deleting books")
    @Test(
            description = "Authorized user deletes a book from collection",
            groups = {"regression", "ui", "e2e"}
    )
    @Description("""
            Precondition: user is created via API, token is generated, 1 book is added to collection.
            UI: profile is opened via cookie injection, book is deleted via Trash icon
            with confirmation in modal window.
            Expected: book disappears from table on UI, and books array in GET /User becomes empty.
            """)
    public void deleteBookFromCollection() {
        createTestUserViaApi();

        Book book = Books.GIT_POCKET_GUIDE;
        AuthSession.injectAuthCookies(testUserId, testUserName, testUserToken, testUserTokenExpires);
        bookApiClient.addBookToUser(testUserId, testUserToken, book.isbn());

        new ProfilePage()
                .open()
                .shouldBeOpened()
                .shouldBookVisible(book.isbn())
                .deleteBook(book.isbn())
                .shouldBookNotBeVisible(book.isbn());

        List<String> booksAfterDeletion = userApiClient.getUserBookIsbns(testUserId, testUserToken);

        assertThat(booksAfterDeletion)
                .as("Book with ISBN %s should disappear from user collection after deletion via UI", book.isbn())
                .doesNotContain(book.isbn());

        assertThat(booksAfterDeletion)
                .as("User books array should be empty — only 1 book (ISBN %s) was in collection before deletion", book.isbn())
                .isEmpty();
    }
}