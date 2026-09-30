package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateUserResponse(
        @JsonProperty("userID") String userID,
        @JsonProperty("username") String username
) {
}