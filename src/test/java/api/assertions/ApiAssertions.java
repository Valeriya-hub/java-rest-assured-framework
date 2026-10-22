package api.assertions;

import api.model.ErrorResponse;
import config.constants.ApiError;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Reusable assertions for API responses, shared by all API tests.
 * Add new response checks here instead of repeating them in tests.
 */
public class ApiAssertions {
    private static final String UUID_REGEX =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private ApiAssertions() {
    }

    /**
     * Verifies that a userID is not blank and has the UUID format.
     * Standalone variant: fails immediately.
     */
    public static void assertValidUserId(String userId) {
        assertSoftly(softly -> assertValidUserId(softly, userId));
    }

    /**
     * Same check, but recorded in the given SoftAssertions, so it can be combined
     * with other checks of the same response (all mismatches are reported at once).
     */
    public static void assertValidUserId(SoftAssertions softly, String userId) {
        softly.assertThat(userId)
                .as("userID")
                .isNotBlank()
                .matches(UUID_REGEX);
    }

    /**
     * Verifies that the response is the expected API error: status, error code and message.
     * All three are checked together, so one run reports every mismatch.
     */
    public static void assertApiError(Response response, ApiError expected) {
        ErrorResponse actual = response.as(ErrorResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(response.statusCode())
                    .as("HTTP status code")
                    .isEqualTo(expected.status());
            softly.assertThat(actual.code())
                    .as("API error code")
                    .isEqualTo(expected.code());
            softly.assertThat(actual.message())
                    .as("API error message")
                    .isEqualTo(expected.message());
        });
    }

    public static void assertExpiresInFuture(SoftAssertions softly, String expires) {
        softly.assertThat(expires).as("expires").isNotBlank();
        if (expires == null || expires.isBlank()) {
            return;
        }
        try {
            softly.assertThat(Instant.parse(expires))
                    .as("expires")
                    .isAfter(Instant.now());
        } catch (DateTimeParseException e) {
            softly.fail("expires is not an ISO-8601 timestamp: " + expires);
        }
    }

    /**
     * Verifies that a failed login did not authenticate anyone.
     *
     * Known deviation of the application under test: it reports a failed login with
     * HTTP 200 and an EMPTY body instead of an error status (see README, Findings).
     * The behaviour is pinned here, in one place, so the tests stay green and still notice
     * when it changes. The security-relevant part is that no token or user data is returned.
     */
    public static void assertLoginNotAuthenticated(Response response) {
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode())
                    .as("HTTP status code (known deviation: 200 instead of an error status)")
                    .isEqualTo(200);
            softly.assertThat(response.asString())
                    .as("Response body: no token or user data may be returned")
                    .isBlank();
        });
    }

    /**
     * Verifies that invalid input is handled gracefully: no server error
     * and no internal details (stack trace, file paths, framework names) in the body.
     */
    public static void assertHandledGracefully(Response response) {
        String body = response.asString();
        assertSoftly(softly -> {
            softly.assertThat(response.statusCode())
                    .as("HTTP status code: invalid input must not cause a server error")
                    .isLessThan(500);
            softly.assertThat(body)
                    .as("Response body must not expose internals (stack trace, file paths, framework)")
                    .doesNotContainIgnoringCase("node_modules")
                    .doesNotContainIgnoringCase("sequelize")
                    .doesNotContain("    at ");
        });
    }
}