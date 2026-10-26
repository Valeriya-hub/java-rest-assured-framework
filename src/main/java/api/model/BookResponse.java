package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A book of the user's collection as returned by GET /Account/v1/User/{userId}.
 * Only the fields the tests verify are mapped.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookResponse(
        @JsonProperty("isbn") String isbn,
        @JsonProperty("title") String title,
        @JsonProperty("author") String author,
        @JsonProperty("publisher") String publisher
) {
}