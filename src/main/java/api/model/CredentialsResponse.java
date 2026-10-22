package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CredentialsResponse(
        @JsonProperty("userID") String userID,
        @JsonProperty("username") String username
) {
}