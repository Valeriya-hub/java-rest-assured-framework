package config.constants;

/**
 * Expected outcome of an authorization attempt, as reported in the response body.
 * Contains standard catalog of authorization outcomes.
 */
public record AuthResult(String status, String result) {

    public static final AuthResult AUTHORIZED =
            new AuthResult("Success", "User authorized successfully.");

    public static final AuthResult AUTHORIZATION_FAILED =
            new AuthResult("Failed", "User authorization failed.");
}