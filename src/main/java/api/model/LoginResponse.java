package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Successful response of POST /Account/v1/Login.
 * Note: the id field is "userId" here, while the create user response uses "userID",
 * so CreateUserResponse cannot be reused.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(
        @JsonProperty("userId") String userId,
        @JsonProperty("username") String username,
        @JsonProperty("token") String token,
        @JsonProperty("expires") String expires
) {
}