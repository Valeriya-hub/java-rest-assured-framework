package tests.api;

import api.client.BookApiClient;
import api.model.AddBookRequest;
import api.model.AddBookResponse;
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

import static api.assertions.ApiAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class AddBookApiTest extends BaseApiTest {

    private static final Book BOOK = Book.GIT_POCKET_GUIDE;

    private final BookApiClient bookApiClient = new BookApiClient();

    /**
     * Builds the request body as a Map, so a field can be empty or absent
     * (the AddBookRequest record would always serialize every field).
     */
    private static Map<String, Object> addBookBody(String userId, Map<String, String> isbnEntry) {
        return Map.of("userId", userId, "collectionOfIsbns", List.of(isbnEntry));
    }

    @Feature("Book store API")
    @Story("Add book to the user collection")
    @Test(
            description = "Add a book to the collection of an authorized user",
            groups = {"api", "smoke"}
    )
    @Description("""
            Precondition: a user is created via API and has a token.
            Request: POST /BookStore/v1/Books with the token, the userId and the ISBN of a reference book.
            Verification: status 201 and a JSON content type; the response contains the added ISBN;
            the book is present in the user's collection (GET /Account/v1/User/{userId}).
            """)
    public void addBookToUserCollection() {
        createTestUserViaApi();
        TestUser user = testUser();

        Response response = bookApiClient.sendAddBookRequest(user.token(),
                new AddBookRequest(user.id(), List.of(new AddBookRequest.IsbnEntry(BOOK.isbn()))));

        // Hard check first: the body can be mapped only for a successful response.
        assertThat(response.statusCode())
                .as("Status code of the add book request")
                .isEqualTo(201);

        AddBookResponse body = response.as(AddBookResponse.class);
        List<String> collectionIsbns = userApiClient.getUserBookIsbns(user.id(), user.token());

        assertSoftly(softly -> {
            assertJsonContentType(softly, response);
            softly.assertThat(body.books())
                    .extracting(AddBookResponse.IsbnEntry::isbn)
                    .as("ISBNs in the add book response")
                    .containsExactly(BOOK.isbn());
            softly.assertThat(collectionIsbns)
                    .as("ISBNs in the user's collection")
                    .containsExactly(BOOK.isbn());
        });
    }

    @Feature("Book store API")
    @Story("Add book - negative scenarios")
    @Test(
            description = "A user cannot add a book to the collection of another user",
            groups = {"api", "regression"}
    )
    @Description("""
        Precondition: user A is created via API and has an empty collection;
        user B is a separate user with their own valid token.
        Request: POST /BookStore/v1/Books with the token of user B, the userId of user A
        and the ISBN of a reference book.
        Verification: the request is rejected with status 401 and the 'User not authorized!' error (code 1200);
        the collection of user A is still empty (nothing was added).
        """)
    public void addBookToAnotherUserCollection() {
        createTestUserViaApi();
        TestUser owner = testUser();
        TestUser anotherUser = createExtraUser();

        // The userId belongs to the owner, but the token belongs to another user.
        Response response = bookApiClient.sendAddBookRequest(anotherUser.token(),
                new AddBookRequest(owner.id(), List.of(new AddBookRequest.IsbnEntry(BOOK.isbn()))));

        assertApiError(response, ApiError.USER_NOT_AUTHORIZED);
        assertThat(userApiClient.getUserBookIsbns(owner.id(), owner.token()))
                .as("The rejected request must not add a book to the collection of another user")
                .isEmpty();
    }

    @Feature("Book store API")
    @Story("Add book - negative scenarios")
    @Test(
            description = "Adding a book without a token is rejected",
            groups = {"api", "smoke"}
    )
    @Description("""
            Precondition: a user is created via API, so only the token is missing in the request.
            Request: POST /BookStore/v1/Books without the Authorization header.
            Verification: the request is rejected with status 401 and the 'User not authorized!' error (code 1200).
            """)
    public void addBookWithoutToken() {
        createTestUserViaApi();

        Response response = bookApiClient.sendAddBookRequest(null,
                addBookBody(testUser().id(), Map.of("isbn", BOOK.isbn())));

        assertApiError(response, ApiError.USER_NOT_AUTHORIZED);
    }

    @Feature("Book store API")
    @Story("Add book - negative scenarios")
    @Test(
            description = "Adding a book with an unavailable ISBN is rejected",
            dataProvider = "unavailableIsbnEntries",
            dataProviderClass = BookPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API and has a token.
            Request: POST /BookStore/v1/Books with a non-existent 13-digit ISBN, a malformed ISBN
            and an empty ISBN value.
            Verification: the request is rejected with status 400 and the
            'ISBN supplied is not available in Books Collection!' error (code 1205).
            """)
    public void addBookWithUnavailableIsbn(String scenario, Map<String, String> isbnEntry) {
        // 'scenario' is not used in the request: it only names the case in the test report.
        createTestUserViaApi();

        Response response = bookApiClient.sendAddBookRequest(testUser().token(),
                addBookBody(testUser().id(), isbnEntry));

        assertApiError(response, ApiError.BOOK_ISBN_NOT_AVAILABLE);
    }

    @Feature("Book store API")
    @Story("Add book - negative scenarios")
    @Test(
            description = "Adding a book that is already in the collection is rejected",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API and the book is already added to the collection.
            Request: POST /BookStore/v1/Books with the same ISBN again.
            Verification: the request is rejected with status 400 and the
            'ISBN already present in the User's Collection!' error (code 1210).
            """)
    public void addBookAlreadyInCollection() {
        createTestUserViaApi();
        TestUser user = testUser();

        // Precondition: not the behaviour under test, so the asserting client method is used.
        bookApiClient.addBookToUser(user.id(), user.token(), BOOK.isbn());

        Response response = bookApiClient.sendAddBookRequest(user.token(),
                addBookBody(user.id(), Map.of("isbn", BOOK.isbn())));

        assertApiError(response, ApiError.ISBN_ALREADY_IN_COLLECTION);
    }

    @Feature("Book store API")
    @Story("Add book - negative scenarios")
    @Test(
            description = "Adding a book with an incorrect userId is rejected",
            dataProvider = "incorrectUserIds",
            dataProviderClass = BookPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API and has a token, so only the userId is wrong.
            Request: POST /BookStore/v1/Books with an empty and with a non-existent userId.
            Verification: the request is rejected with status 401 and the 'User Id not correct!' error (code 1207).
            """)
    public void addBookWithIncorrectUserId(String scenario, String userId) {
        // 'scenario' is not used in the request: it only names the case in the test report.
        createTestUserViaApi();

        Response response = bookApiClient.sendAddBookRequest(testUser().token(),
                addBookBody(userId, Map.of("isbn", BOOK.isbn())));

        assertApiError(response, ApiError.USER_ID_NOT_CORRECT);
    }

    @Feature("Book store API")
    @Story("Add book - negative scenarios")
    @Test(
            description = "Adding a book with an empty collection of ISBNs is rejected",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API and has a token.
            Request: POST /BookStore/v1/Books with the userId and an empty collectionOfIsbns array.
            Verification: the request is rejected with status 400 and the
            'Collection of books required.' error (code 1207).
            """)
    public void addBookWithEmptyCollection() {
        createTestUserViaApi();

        Response response = bookApiClient.sendAddBookRequest(testUser().token(),
                Map.of("userId", testUser().id(), "collectionOfIsbns", List.of()));

        assertApiError(response, ApiError.COLLECTION_OF_BOOKS_REQUIRED);
    }

    @Feature("Book store API")
    @Story("Add book - negative scenarios")
    @Test(
            description = "Adding a book without the userId field is rejected with a validation error (KNOWN ISSUE)",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API and has a token.
            Request: POST /BookStore/v1/Books without the 'userId' field in the body.
            Verification (desired behaviour): the input is handled gracefully (no server error,
            no internal details in the body) and rejected with the 'User Id not correct!' error.
            
            Known issue: the application crashes with HTTP 500 and returns a stack trace
            (README, Findings #9). Until it is fixed the test is reported as skipped;
            when the behaviour is fixed, the test fails to remind that the marker must be removed.
            """)
    public void addBookWithoutUserId() {
        createTestUserViaApi();

        Response response = bookApiClient.sendAddBookRequest(testUser().token(),
                Map.of("collectionOfIsbns", List.of(Map.of("isbn", BOOK.isbn()))));

        KnownIssue.expectFailure("README, Findings #9", () -> {
            assertHandledGracefully(response);
            assertApiError(response, ApiError.USER_ID_NOT_CORRECT);
        });
    }

    @Feature("Book store API")
    @Story("Add book - negative scenarios")
    @Test(
            description = "Adding a book without the collectionOfIsbns field is rejected with a validation error (KNOWN ISSUE)",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API and has a token.
            Request: POST /BookStore/v1/Books with the userId and without the 'collectionOfIsbns' field.
            Verification (desired behaviour): the input is handled gracefully (no server error,
            no internal details in the body) and rejected with the 'Collection of books required.' error.
            
            Known issue: the application crashes with HTTP 500 and returns a stack trace
            (README, Findings #10). Until it is fixed the test is reported as skipped;
            when the behaviour is fixed, the test fails to remind that the marker must be removed.
            """)
    public void addBookWithoutCollectionOfIsbns() {
        createTestUserViaApi();

        Response response = bookApiClient.sendAddBookRequest(testUser().token(),
                Map.of("userId", testUser().id()));

        KnownIssue.expectFailure("README, Findings #10", () -> {
            assertHandledGracefully(response);
            assertApiError(response, ApiError.COLLECTION_OF_BOOKS_REQUIRED);
        });
    }
}