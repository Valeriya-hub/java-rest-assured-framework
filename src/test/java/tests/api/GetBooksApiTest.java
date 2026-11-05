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

import static api.assertions.ApiAssertions.assertJsonContentType;
import static api.assertions.ApiAssertions.assertMatchesSchema;
import static api.assertions.BookAssertions.assertValidBooksCollection;
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
            (every book has isbn, title, subTitle, author, publish_date, publisher, pages,
            description and website with the correct types); the books collection is not empty,
            ISBNs are unique, subTitle, description and website of every book are not blank
            and the website is a URL; the books the tests rely on are present with the reference data.
            """)
    public void getAllBooks() {
        Response response = bookApiClient.sendGetBooksRequest();

        // Hard checks first: the rest makes sense only for a successful, well-formed response.
        assertThat(response.statusCode())
                .as("Status code of the get all books request")
                .isEqualTo(200);
        assertMatchesSchema(response, BOOKS_SCHEMA);

        BooksResponse body = response.as(BooksResponse.class);

        Tuple[] referenceBooks = Book.all().stream()
                .map(book -> tuple(book.isbn(), book.title(), book.author(), book.publisher()))
                .toArray(Tuple[]::new);

        assertSoftly(softly -> {
            assertJsonContentType(softly, response);
            assertValidBooksCollection(softly, body.books());
            softly.assertThat(body.books())
                    .extracting(BookResponse::isbn, BookResponse::title,
                            BookResponse::author, BookResponse::publisher)
                    .as("reference books are present with the reference data")
                    .contains(referenceBooks);
        });
    }
}
