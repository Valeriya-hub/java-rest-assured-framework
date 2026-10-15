package data;

/**
 * The user created for the current test: identity, credentials and the token used for cleanup.
 * Immutable: a refreshed token is expressed with withToken(), which returns a new instance.
 */
public record TestUser(String id, String name, String password, String token, String tokenExpires) {

    public TestUser withToken(String newToken, String newTokenExpires) {
        return new TestUser(id, name, password, newToken, newTokenExpires);
    }
}