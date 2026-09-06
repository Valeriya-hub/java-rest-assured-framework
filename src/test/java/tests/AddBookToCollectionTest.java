package tests;

import api.client.UserApiClient;
import org.testng.annotations.Test;
import ui.BookPage;
import ui.BookStorePage;
import ui.LoginPage;
import ui.ProfilePage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AddBookToCollectionTest extends BaseTest {
    @Test(description = "Авторизований користувач додає книгу в колекцію")
    public void authorizedUserAddBookToCollection() {
        createTestUserViaApi();

        ProfilePage profilePage = new LoginPage()
                .open()
                .loginAs(testUserName, testUserPassword);
        testUserToken = profilePage.getAuthTokenFromCookie();

        BookStorePage bookStorePage = profilePage.goToBookStore();
        BookPage bookPage = bookStorePage.clickOnBook();
        bookPage.clickAddToCollectionButton();
        bookPage.dismissAlertIfPresent();
        String addedBookIsbn = bookPage.getIsbn();
        UserApiClient userApiClientWithAuth = new UserApiClient(testUserId, testUserToken);
        List<String> userBookIsbns = userApiClientWithAuth.getUserBookIsbns();

        assertThat(userBookIsbns)
                .as("Книга з ISBN %s має з'явитись в колекції користувача після додавання через UI", addedBookIsbn)
                .contains(addedBookIsbn);
    }
}
