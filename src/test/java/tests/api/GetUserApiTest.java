package tests.api;

import api.client.BookApiClient;
import api.model.BookResponse;
import api.model.CredentialsRequest;
import api.model.CredentialsResponse;
import api.model.UserDetailsResponse;
import config.constants.ApiError;
import data.Book;
import data.TestUser;
import data.UserFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.base.BaseApiTest;

import java.util.UUID;

import static api.assertions.ApiAssertions.assertApiError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class GetUserApiTest extends BaseApiTest {

    private final BookApiClient bookApiClient = new BookApiClient();

    @Feature("User account API")
    @Story("Get user")
    @Test(
            description = "Get user information with a book in the collection",
            groups = {"api", "smoke"}
    )
    @Description("""
            Precondition: a user is created via API and one book is added to the collection.
            Request: GET /Account/v1/User/{userId} with the user's token.
            Verification: status 200; the body contains the username of the user and
            a books collection with exactly the added book (isbn, title, author, publisher).
            """)
    public void getUserInformationWithBook() {
        CredentialsRequest user = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        Book book = Book.GIT_POCKET_GUIDE;
        bookApiClient.addBookToUser(created.userID(), testUser().token(), book.isbn());

        Response response = userApiClient.sendGetUserRequest(created.userID(), testUser().token());

        // Hard check first: without a successful response there is no body worth inspecting.
        assertThat(response.statusCode())
                .as("Status code of the get user request")
                .isEqualTo(200);

        UserDetailsResponse body = response.as(UserDetailsResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(body.username())
                    .as("username")
                    .isEqualTo(user.userName());

            // One assertion checks both the size and the content of the collection.
            softly.assertThat(body.books())
                    .as("books collection")
                    .extracting(BookResponse::isbn, BookResponse::title,
                            BookResponse::author, BookResponse::publisher)
                    .containsExactly(tuple(book.isbn(), book.title(), book.author(), book.publisher()));
        });
    }

    @Feature("User account API")
    @Story("Get user")
    @Test(
            description = "Get information of a new user: the books collection is empty",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API, no books are added.
            Request: GET /Account/v1/User/{userId} with the user's token.
            Verification: status 200; the body contains the username of the user
            and an empty books collection.
            """)
    public void getNewUserInformationHasEmptyBooksCollection() {
        CredentialsRequest user = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        Response response = userApiClient.sendGetUserRequest(created.userID(), testUser().token());

        assertThat(response.statusCode())
                .as("Status code of the get user request")
                .isEqualTo(200);

        UserDetailsResponse body = response.as(UserDetailsResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(body.username())
                    .as("username")
                    .isEqualTo(user.userName());
            softly.assertThat(body.books())
                    .as("books collection")
                    .isNotNull()
                    .isEmpty();
        });
    }

    @Feature("User account API")
    @Story("Get user - negative scenarios")
    @Test(
            description = "Get user information without a token is rejected",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API.
            Request: GET /Account/v1/User/{userId} without any authorization.
            Verification: the request is rejected with 401, code 1200, 'User not authorized!'.
            """)
    public void getUserWithoutTokenIsRejected() {
        CredentialsRequest user = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        // The id is valid, so the missing token is the only reason for the rejection.
        Response response = userApiClient.sendGetUserRequestWithoutToken(created.userID());

        assertApiError(response, ApiError.USER_NOT_AUTHORIZED);
    }

    // The two scenarios below return an identical response, so the API neither leaks the data
    // of another user nor reveals whether an id exists. The status 401 (instead of 403 or 404)
    // is a known deviation, see README, Findings.

    @Feature("User account API")
    @Story("Get user - negative scenarios")
    @Test(
            description = "A user cannot get the information of another user",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: two users are created via API.
            Request: GET /Account/v1/User/{userId} with the token of the first user and the id of the second.
            Verification: the request is rejected with 401, code 1207, 'User not found!',
            and nothing about the second user is returned.
            
            Known deviation: 401 instead of 403 or 404 (README, Findings).
            """)
    public void getAnotherUsersInformationIsRejected() {
        CredentialsRequest user = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());
        TestUser otherUser = createExtraUser();

        // An authenticated user asks for the data of ANOTHER user.
        Response response = userApiClient.sendGetUserRequest(otherUser.id(), testUser().token());

        assertApiError(response, ApiError.USER_NOT_AUTHORIZED);
        assertThat(response.asString())
                .as("Response must not contain any data of the other user")
                .doesNotContain(otherUser.name());
    }

    @Feature("User account API")
    @Story("Get user - negative scenarios")
    @Test(
            description = "Get information of a non-existing user is rejected",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API.
            Request: GET /Account/v1/User/{userId} with a valid token and an id that does not exist.
            Verification: the request is rejected with 401, code 1207, 'User not found!'.
            
            Known deviation: 401 instead of 404 (README, Findings).
            """)
    public void getNonExistingUserIsRejected() {
        CredentialsRequest user = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        // A well-formed UUID that was never registered.
        String unknownUserId = UUID.randomUUID().toString();

        Response response = userApiClient.sendGetUserRequest(unknownUserId, testUser().token());

        assertApiError(response, ApiError.USER_NOT_FOUND);
    }
}