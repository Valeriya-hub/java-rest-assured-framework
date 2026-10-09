package tests;

import auth.AuthSession;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import ui.BookPage;
import ui.BookStorePage;
import ui.ProfilePage;

import static org.assertj.core.api.Assertions.assertThat;

public class AddBookToCollectionTest extends BaseTest {

    @Feature("Book collection management")
    @Story("Adding books")
    @Test(
            description = "User adds a book to collection",
            groups = {"regression", "ui", "e2e"}
    )
    @Description("""
            Precondition: user is created via API, auth cookies are injected into browser.
            UI: Profile is opened -> navigate to Book Store -> select book 'Git Pocket Guide' ->
            click 'Add To Collection' and close alert.
            Verification: via API (GET /Account/v1/User) verify that the ISBN of the added book
            appeared in the user's collection.
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
                .as("Book with ISBN %s should appear in user collection after adding via UI", addedBookIsbn)
                .contains(addedBookIsbn);
    }
}
