package api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateTokenResponse(
        @JsonProperty("token") String token,
        @JsonProperty("expires") String expires,
        @JsonProperty("status") String status,
        @JsonProperty("result") String result
) {
}