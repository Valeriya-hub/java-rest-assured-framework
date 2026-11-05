package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A book as returned by GET /BookStore/v1/Books and by GET /Account/v1/User/{userId}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookResponse(
        @JsonProperty("isbn") String isbn,
        @JsonProperty("title") String title,
        @JsonProperty("subTitle") String subTitle,
        @JsonProperty("author") String author,
        @JsonProperty("publish_date") String publishDate,
        @JsonProperty("publisher") String publisher,
        @JsonProperty("pages") Integer pages,
        @JsonProperty("description") String description,
        @JsonProperty("website") String website
) {
}