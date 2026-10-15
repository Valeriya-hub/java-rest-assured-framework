package tests.base;

import api.model.GenerateTokenResponse;
import data.TestUser;

/**
 * Base class for API tests. Holds helpers shared by all API tests,
 * so individual tests stay focused on the behaviour they verify.
 */
public class BaseApiTest extends BaseTest {

    /**
     * Remembers a user that was created inside a test body, so that
     * BaseTest.tearDown() deletes it after the test.
     * <p>
     * Call it right after the user is created and BEFORE any further assertion:
     * a failing assertion must not leave the user behind.
     * A token is required because deleting a user is an authorized call.
     */
    protected void registerCreatedUserForCleanup(String userId, String userName, String password) {
        // Registered before the token is requested, so the user is known even if that call fails.
        setTestUser(new TestUser(userId, userName, password, null, null));

        GenerateTokenResponse tokenResponse = userApiClient.generateTokenDetails(userName, password);
        updateTestUserToken(tokenResponse.token(), tokenResponse.expires());
    }
}