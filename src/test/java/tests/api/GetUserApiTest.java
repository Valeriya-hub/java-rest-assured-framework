package tests.api;

import api.client.BookApiClient;
import api.model.BookResponse;
import api.model.CredentialsRequest;
import api.model.CredentialsResponse;
import api.model.UserDetailsResponse;
import data.Book;
import data.UserFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.base.BaseApiTest;

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
}