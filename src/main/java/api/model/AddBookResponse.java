package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response of POST /BookStore/v1/Books: the ISBNs that were added to the user's collection.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AddBookResponse(@JsonProperty("books") List<IsbnEntry> books) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IsbnEntry(@JsonProperty("isbn") String isbn) {
    }
}