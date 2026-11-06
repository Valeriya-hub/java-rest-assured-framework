package api.assertions;

import api.model.BookResponse;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Reusable assertions for the BookStore books, shared by all tests that read books.
 */
public class BookAssertions {

    private BookAssertions() {
    }

    /**
     * Invariants of any books collection returned by the API:
     * not empty, unique ISBNs, and every book has its details filled.
     */
    public static void assertValidBooksCollection(SoftAssertions softly, List<BookResponse> books) {
        softly.assertThat(books)
                .as("books collection")
                .isNotEmpty();
        softly.assertThat(books)
                .extracting(BookResponse::isbn)
                .as("ISBNs of the books")
                .doesNotHaveDuplicates();

        books.forEach(book -> assertBookDetailsFilled(softly, book));
    }

    private static void assertBookDetailsFilled(SoftAssertions softly, BookResponse book) {
        String id = "book[" + book.isbn() + "] ";

        softly.assertThat(book.subTitle()).as(id + "subTitle").isNotBlank();
        softly.assertThat(book.description()).as(id + "description").isNotBlank();
        softly.assertThat(book.website()).as(id + "website").isNotBlank().startsWith("http");
    }

    /**
     * Verifies the current behaviour of GET /BookStore/v1/Book without the ISBN parameter.
     * <p>
     * Known deviation of the application under test: the server fails with HTTP 500 and an HTML
     * error page instead of a 400 JSON error (see README, Findings). The body is NOT parsed
     * as ErrorResponse, because it is not JSON. Internals in the body are deliberately not
     * asserted: the page text is not part of any contract.
     * When the defect is fixed, replace this check with assertApiError or assertHandledGracefully.
     */
    public static void assertMissingIsbnParameterKnownDeviation(Response response) {
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode())
                    .as("HTTP status code (known deviation: 500 instead of 400)")
                    .isEqualTo(500);
            softly.assertThat(response.contentType())
                    .as("Content-Type (known deviation: HTML error page instead of JSON)")
                    .contains("text/html");
        });
    }
}