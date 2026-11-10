package tests.api;

import api.client.BookApiClient;
import api.model.DeleteBookRequest;
import config.constants.ApiError;
import data.Book;
import data.BookPayloadProviders;
import data.TestUser;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.base.BaseApiTest;
import tests.base.KnownIssue;

import java.util.List;
import java.util.Map;

import static api.assertions.ApiAssertions.assertApiError;
import static api.assertions.ApiAssertions.assertHandledGracefully;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class DeleteBookApiTest extends BaseApiTest {
    private static final Book BOOK = Book.GIT_POCKET_GUIDE;
    private static final Book ANOTHER_BOOK = Book.SPEAKING_JAVASCRIPT;

    private final BookApiClient bookApiClient = new BookApiClient();

    @Feature("Book store API")
    @Story("Delete book from the user collection")
    @Test(
            description = "Delete a book from the collection of an authorized user",
            groups = {"api", "smoke"}
    )
    @Description("""
            Precondition: a user is created via API and has two books in the collection.
            Request: DELETE /BookStore/v1/Book with the token, the userId and the ISBN of one of the books.
            Verification: status 204 and an empty body; only the requested book is removed
            (the other one stays in GET /Account/v1/User/{userId}).
            """)
    public void deleteBookFromUserCollection() {
        createTestUserViaApi();
        TestUser user = testUser();
        bookApiClient.addBookToUser(user.id(), user.token(), BOOK.isbn());
        bookApiClient.addBookToUser(user.id(), user.token(), ANOTHER_BOOK.isbn());

        Response response = bookApiClient.sendDeleteBookRequest(user.token(),
                new DeleteBookRequest(user.id(), BOOK.isbn()));

        List<String> collectionIsbns = userApiClient.getUserBookIsbns(user.id(), user.token());

        assertSoftly(softly -> {
            softly.assertThat(response.statusCode())
                    .as("Status code of the delete book request")
                    .isEqualTo(204);
            softly.assertThat(response.asString())
                    .as("Response body of a successful deletion")
                    .isBlank();
            softly.assertThat(collectionIsbns)
                    .as("ISBNs in the user's collection: only the deleted book is gone")
                    .containsExactly(ANOTHER_BOOK.isbn());
        });
    }

    @Feature("Book store API")
    @Story("Delete book - negative scenarios")
    @Test(
            description = "Deleting a book without valid authentication is rejected",
            dataProvider = "unauthorizedTokens",
            dataProviderClass = BookPayloadProviders.class,
            groups = {"api", "smoke"}
    )
    @Description("""
            Precondition: a user is created via API and has a book in the collection,
            so only the authentication is wrong.
            Request: DELETE /BookStore/v1/Book without the Authorization header and with a malformed token.
            Verification: the request is rejected with status 401 and the 'User not authorized!' error (code 1200);
            the book is still in the user's collection (nothing was deleted).
            """)
    public void deleteBookWithoutAuthentication(String scenario, String token) {
        // 'scenario' is not used in the request: it only names the case in the test report.
        createTestUserViaApi();
        TestUser user = testUser();
        bookApiClient.addBookToUser(user.id(), user.token(), BOOK.isbn());

        Response response = bookApiClient.sendDeleteBookRequest(token,
                new DeleteBookRequest(user.id(), BOOK.isbn()));

        assertApiError(response, ApiError.USER_NOT_AUTHORIZED);
        assertThat(userApiClient.getUserBookIsbns(user.id(), user.token()))
                .as("The rejected request must not delete anything")
                .containsExactly(BOOK.isbn());
    }

    @Feature("Book store API")
    @Story("Delete book - negative scenarios")
    @Test(
            description = "Deleting a book that is not in the collection is rejected",
            dataProvider = "isbnsNotInCollection",
            dataProviderClass = BookPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API and has one book in the collection.
            Request: DELETE /BookStore/v1/Book with an ISBN that is not in the collection:
            a catalog book the user does not own, a non-existent 13-digit ISBN and an empty ISBN value.
            Verification: the request is rejected with status 400 and the
            'ISBN supplied is not available in User's Collection!' error (code 1206);
            the user's collection is unchanged.
            """)
    public void deleteBookNotInCollection(String scenario, String isbn) {
        // 'scenario' is not used in the request: it only names the case in the test report.
        createTestUserViaApi();
        TestUser user = testUser();
        bookApiClient.addBookToUser(user.id(), user.token(), BOOK.isbn());

        Response response = bookApiClient.sendDeleteBookRequest(user.token(),
                new DeleteBookRequest(user.id(), isbn));

        assertApiError(response, ApiError.ISBN_NOT_IN_COLLECTION);
        assertThat(userApiClient.getUserBookIsbns(user.id(), user.token()))
                .as("The rejected request must not change the collection")
                .containsExactly(BOOK.isbn());
    }

    @Feature("Book store API")
    @Story("Delete book - negative scenarios")
    @Test(
            description = "Deleting the same book twice: the second request is rejected",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API, the book is added and then deleted.
            Request: DELETE /BookStore/v1/Book with the same ISBN again.
            Verification: the request is rejected with status 400 and the
            'ISBN supplied is not available in User's Collection!' error (code 1206).
            """)
    public void deleteAlreadyDeletedBook() {
        createTestUserViaApi();
        TestUser user = testUser();
        bookApiClient.addBookToUser(user.id(), user.token(), BOOK.isbn());
        bookApiClient.deleteBookFromUser(user.id(), user.token(), BOOK.isbn());

        Response response = bookApiClient.sendDeleteBookRequest(user.token(),
                new DeleteBookRequest(user.id(), BOOK.isbn()));

        assertApiError(response, ApiError.ISBN_NOT_IN_COLLECTION);
    }

    @Feature("Book store API")
    @Story("Delete book - negative scenarios")
    @Test(
            description = "A user cannot delete a book from the collection of another user",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: user A is created via API and has a book in the collection;
            user B is a separate user with their own valid token.
            Request: DELETE /BookStore/v1/Book with the token of user B, the userId of user A
            and the ISBN of the book of user A.
            Verification: the request is rejected with status 401 and the 'User not authorized!' error (code 1200);
            the book is still in the collection of user A (nothing was deleted).
            """)
    public void deleteBookFromAnotherUserCollection() {
        createTestUserViaApi();
        TestUser owner = testUser();
        TestUser anotherUser = createExtraUser();
        bookApiClient.addBookToUser(owner.id(), owner.token(), BOOK.isbn());

        // The userId and the book belong to the owner, but the token belongs to another user.
        Response response = bookApiClient.sendDeleteBookRequest(anotherUser.token(),
                new DeleteBookRequest(owner.id(), BOOK.isbn()));

        assertApiError(response, ApiError.USER_NOT_AUTHORIZED);
        assertThat(userApiClient.getUserBookIsbns(owner.id(), owner.token()))
                .as("The rejected request must not delete the book of another user")
                .containsExactly(BOOK.isbn());
    }

    @Feature("Book store API")
    @Story("Delete book - negative scenarios")
    @Test(
            description = "Deleting a book with an incorrect userId is rejected",
            dataProvider = "incorrectUserIds",
            dataProviderClass = BookPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API, has a token and a book in the collection,
            so only the userId is wrong.
            Request: DELETE /BookStore/v1/Book with an empty and with a non-existent userId.
            Verification: the request is rejected with status 401 and the 'User Id not correct!' error (code 1207);
            the user's collection is unchanged.
            """)
    public void deleteBookWithIncorrectUserId(String scenario, String userId) {
        // 'scenario' is not used in the request: it only names the case in the test report.
        createTestUserViaApi();
        TestUser user = testUser();
        bookApiClient.addBookToUser(user.id(), user.token(), BOOK.isbn());

        Response response = bookApiClient.sendDeleteBookRequest(user.token(),
                new DeleteBookRequest(userId, BOOK.isbn()));

        assertApiError(response, ApiError.USER_ID_NOT_CORRECT);
        assertThat(userApiClient.getUserBookIsbns(user.id(), user.token()))
                .as("The rejected request must not change the collection")
                .containsExactly(BOOK.isbn());
    }

    @Feature("Book store API")
    @Story("Delete book - negative scenarios")
    @Test(
            description = "Deleting a book without the isbn field is rejected with a validation error (KNOWN ISSUE)",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API, has a token and a book in the collection.
            Request: DELETE /BookStore/v1/Book with the userId and without the 'isbn' field in the body.
            Verification (desired behaviour): the input is handled gracefully (no server error,
            no internal details in the body) and rejected with the same error as an empty ISBN:
            400 'ISBN supplied is not available in User's Collection!' (code 1206).
            
            Known issue: the application crashes with HTTP 500 and returns an HTML page with a stack trace
            (README, Findings #12). Until it is fixed the test is reported as skipped;
            when the behaviour is fixed, the test fails to remind that the marker must be removed.
            """)
    public void deleteBookWithoutIsbn() {
        createTestUserViaApi();
        TestUser user = testUser();
        bookApiClient.addBookToUser(user.id(), user.token(), BOOK.isbn());

        Response response = bookApiClient.sendDeleteBookRequest(user.token(),
                Map.of("userId", user.id()));

        KnownIssue.expectFailure("README, Findings #12", () -> {
            assertHandledGracefully(response);
            assertApiError(response, ApiError.ISBN_NOT_IN_COLLECTION);
        });
    }

    @Feature("Book store API")
    @Story("Delete book - negative scenarios")
    @Test(
            description = "Deleting a book without the userId field is rejected with a validation error (KNOWN ISSUE)",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API, has a token and a book in the collection.
            Request: DELETE /BookStore/v1/Book with the isbn and without the 'userId' field in the body.
            Verification (desired behaviour): the input is handled gracefully (no server error,
            no internal details in the body) and rejected with the same error as an empty userId:
            401 'User Id not correct!' (code 1207).
            
            Known issue: the application crashes with HTTP 500 and returns an HTML page with a stack trace
            (README, Findings #13). Until it is fixed the test is reported as skipped;
            when the behaviour is fixed, the test fails to remind that the marker must be removed.
            """)
    public void deleteBookWithoutUserId() {
        createTestUserViaApi();
        TestUser user = testUser();
        bookApiClient.addBookToUser(user.id(), user.token(), BOOK.isbn());

        Response response = bookApiClient.sendDeleteBookRequest(user.token(),
                Map.of("isbn", BOOK.isbn()));

        KnownIssue.expectFailure("README, Findings #13", () -> {
            assertHandledGracefully(response);
            assertApiError(response, ApiError.USER_ID_NOT_CORRECT);
        });
    }
}