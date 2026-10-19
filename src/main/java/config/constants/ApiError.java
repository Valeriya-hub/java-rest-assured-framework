package config.constants;

/**
 * Expected API error: HTTP status together with the error code and message from the body.
 * Contains standard catalog of expected API errors.
 */
public record ApiError(int status, String code, String message) {

    // Observed demoqa behaviour when the username is already taken.
    public static final ApiError USER_EXISTS =
            new ApiError(406, "1204", "User exists!");

    public static final ApiError USER_NAME_AND_PASSWORD_REQUIRED =
            new ApiError(400, "1200", "UserName and Password required.");
}