package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error body returned by the demoqa Account/BookStore APIs,
 * e.g. {"code":"1204","message":"User exists!"}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message
) {
}