package data;

import java.util.Map;

/**
 * Invalid request bodies for endpoints that take userName and password
 * (create user, generate token). Built as Maps, so that a missing field is really
 * absent from the JSON, not sent as null.
 */
public class UserPayloads {
    private UserPayloads() {
    }

    public static Map<String, String> withoutUserName() {
        return Map.of("password", UserFactory.VALID_PASSWORD);
    }

    public static Map<String, String> withoutPassword() {
        return Map.of("userName", UserFactory.randomCredentials().userName());
    }

    public static Map<String, String> withoutAnyField() {
        return Map.of();
    }

    public static Map<String, String> withEmptyUserName() {
        return Map.of("userName", "", "password", UserFactory.VALID_PASSWORD);
    }

    public static Map<String, String> withEmptyPassword() {
        return Map.of("userName", UserFactory.randomCredentials().userName(), "password", "");
    }

    public static Map<String, String> withEmptyFields() {
        return Map.of("userName", "", "password", "");
    }
}
