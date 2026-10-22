package data;

import org.testng.annotations.DataProvider;

/**
 * Shared data providers with invalid credentials bodies. Every endpoint that takes
 * userName and password (create user, login, ...) validates its input on its own,
 * so each one is tested with the same scenarios. The scenarios live here once:
 * a test class only references them via dataProviderClass.
 */
public class UserPayloadProviders {
    private UserPayloadProviders() {
    }

    @DataProvider(name = "missingFieldPayloads")
    public static Object[][] missingFieldPayloads() {
        return new Object[][]{
                {"userName is missing", UserPayloads.withoutUserName()},
                {"password is missing", UserPayloads.withoutPassword()},
                {"both fields are missing", UserPayloads.withoutAnyField()}
        };
    }

    @DataProvider(name = "emptyValuePayloads")
    public static Object[][] emptyValuePayloads() {
        return new Object[][]{
                {"userName is empty", UserPayloads.withEmptyUserName()},
                {"password is empty", UserPayloads.withEmptyPassword()},
                {"both values are empty", UserPayloads.withEmptyFields()}
        };
    }
}
