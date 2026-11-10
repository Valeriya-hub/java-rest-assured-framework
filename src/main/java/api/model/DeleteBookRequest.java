package api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Body of DELETE /BookStore/v1/Book: removes ONE book from the user's collection.
 * (Unlike POST /Books, the ISBN is a plain field here, not a list.)
 */
public record DeleteBookRequest(
        @JsonProperty("userId") String userId,
        @JsonProperty("isbn") String isbn) {
}