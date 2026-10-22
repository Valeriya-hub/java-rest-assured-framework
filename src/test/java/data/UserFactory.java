package data;

import api.model.CredentialsRequest;

import java.util.UUID;

/**
 * Factory of unique credentials for a user that is about to be created.
 * Users are mutable entities, so every test generates its own credentials instead of
 * sharing one user (tests stay independent and can run in any order or in parallel).
 * The user that already exists on the server is represented by TestUser.
 */
public class UserFactory {
    public static final String VALID_PASSWORD = "StrongPass123!";
    public static final String WRONG_PASSWORD = "WrongPassword123!";

    private UserFactory() {
    }

    public static CredentialsRequest randomCredentials() {
        String userName = "qa_user_" + UUID.randomUUID().toString().substring(0, 8);
        return new CredentialsRequest(userName, VALID_PASSWORD);
    }
}