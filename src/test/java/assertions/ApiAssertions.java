package assertions;

import api.model.ErrorResponse;
import config.constants.ApiError;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;

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
}