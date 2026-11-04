package tests.api;

import api.client.BookApiClient;
import api.model.BookResponse;
import api.model.BooksResponse;
import data.Book;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.assertj.core.groups.Tuple;
import org.testng.annotations.Test;
import tests.base.BaseApiTest;

import static api.assertions.ApiAssertions.assertMatchesSchema;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class GetBooksApiTest extends BaseApiTest {
    private static final String BOOKS_SCHEMA = "schemas/books-response.json";

    private final BookApiClient bookApiClient = new BookApiClient();

    @Feature("Book store API")
    @Story("Get all books")
    @Test(
            description = "Get all books",
            groups = {"api", "smoke"}
    )
    @Description("""
            Request: GET /BookStore/v1/Books (a public endpoint, no token is needed).
            Verification: status 200 and a JSON content type; the body matches the JSON schema
            (structure, required fields and their types); the books collection is not empty,
            ISBNs are unique, and the books the tests rely on are present with the reference data.
            """)
    public void getAllBooks() {
        Response response = bookApiClient.sendGetBooksRequest();

        // Hard checks first: the rest makes sense only for a successful, well-formed response.
        assertThat(response.statusCode())
                .as("Status code of the get all books request")
                .isEqualTo(200);
        assertMatchesSchema(response, BOOKS_SCHEMA);

        BooksResponse body = response.as(BooksResponse.class);

        // The books of the reference catalog (data.Books) must be in the response unchanged.
        Tuple[] referenceBooks = Book.all().stream()
                .map(book -> tuple(book.isbn(), book.title(), book.author(), book.publisher()))
                .toArray(Tuple[]::new);

        assertSoftly(softly -> {
            softly.assertThat(response.contentType())
                    .as("Content-Type")
                    .contains("application/json");
            softly.assertThat(body.books())
                    .as("books collection")
                    .isNotEmpty();
            softly.assertThat(body.books())
                    .extracting(BookResponse::isbn)
                    .as("ISBNs of the books")
                    .doesNotHaveDuplicates();
            softly.assertThat(body.books())
                    .extracting(BookResponse::isbn, BookResponse::title,
                            BookResponse::author, BookResponse::publisher)
                    .as("reference books are present with the reference data")
                    .contains(referenceBooks);
        });
    }
}
