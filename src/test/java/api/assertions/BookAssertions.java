package api.assertions;

import api.model.BookResponse;
import org.assertj.core.api.SoftAssertions;

import java.util.List;

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
}