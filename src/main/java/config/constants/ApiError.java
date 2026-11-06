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

    public static final ApiError USER_NOT_AUTHORIZED =
            new ApiError(401, "1200", "User not authorized!");

    public static final ApiError USER_NOT_FOUND =
            new ApiError(401, "1207", "User not found!");

    public static final ApiError BOOK_ISBN_NOT_AVAILABLE =
            new ApiError(400, "1205", "ISBN supplied is not available in Books Collection!");

    public static final ApiError USER_ID_NOT_CORRECT =
            new ApiError(401, "1207", "User Id not correct!");

    public static final ApiError ISBN_ALREADY_IN_COLLECTION =
            new ApiError(400, "1210", "ISBN already present in the User's Collection!");

    public static final ApiError COLLECTION_OF_BOOKS_REQUIRED =
            new ApiError(400, "1207", "Collection of books required.");
}