package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response of GET /Account/v1/User/{userId}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDetailsResponse(
        @JsonProperty("username") String username,
        @JsonProperty("books") List<BookResponse> books
) {
}