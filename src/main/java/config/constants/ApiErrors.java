package config.constants;

/**
 * Catalog of expected API errors, shared by all API tests.
 * Add a new error here, in one place, the first time a test needs it.
 */
public class ApiErrors {
    // Observed demoqa behaviour when the username is already taken.
    public static final ApiError USER_EXISTS =
            new ApiError(
                    406,
                    "1204",
                    "User exists!");

    public static final ApiError USER_NAME_AND_PASSWORD_REQUIRED =
            new ApiError(
                    400,
                    "1200",
                    "UserName and Password required."
            );


    private ApiErrors() {
    }
}