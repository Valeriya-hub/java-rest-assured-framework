package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response of GET /BookStore/v1/Books: the catalog of all books.
 * Only the fields the tests verify are mapped; the full structure is described
 * by the JSON schema schemas/books-response.json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BooksResponse(
        @JsonProperty("books") List<BookResponse> books
) {
}
