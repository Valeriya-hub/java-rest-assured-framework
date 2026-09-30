package api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AddBookRequest(
        @JsonProperty("userId") String userId,
        @JsonProperty("collectionOfIsbns") List<IsbnEntry> collectionOfIsbns) {

    public record IsbnEntry(@JsonProperty("isbn") String isbn) {
    }
}