package tests.base;

import api.model.CredentialsRequest;
import api.model.CredentialsResponse;
import api.model.GenerateTokenResponse;
import data.TestUser;
import data.UserFactory;
import org.testng.annotations.AfterMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for API tests. Holds helpers shared by all API tests,
 * so individual tests stay focused on the behaviour they verify.
 */
public abstract class BaseApiTest extends BaseTest {

    // Additional users of the test running on the current thread (see createExtraUser()).
    // A ThreadLocal, because tests may run in parallel.
    private static final ThreadLocal<List<TestUser>> EXTRA_USERS =
            ThreadLocal.withInitial(ArrayList::new);

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

    /**
     * Creates one more user for scenarios that need two users, e.g. requesting the data
     * of ANOTHER user. The primary test user (testUser()) is not affected.
     * The extra user is deleted after the test.
     */
    protected TestUser createExtraUser() {
        CredentialsRequest credentials = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(credentials.userName(), credentials.password());
        GenerateTokenResponse tokenResponse =
                userApiClient.generateTokenDetails(credentials.userName(), credentials.password());

        TestUser extraUser = new TestUser(created.userID(), credentials.userName(),
                credentials.password(), tokenResponse.token(), tokenResponse.expires());
        EXTRA_USERS.get().add(extraUser);
        return extraUser;
    }

    // A subclass @AfterMethod runs before the superclass one (BaseTest.tearDown),
    // so the extra users are removed first.
    @AfterMethod(alwaysRun = true)
    public void cleanUpExtraUsers() {
        try {
            for (TestUser user : EXTRA_USERS.get()) {
                userApiClient.deleteUser(user.id(), user.token());
            }
        } finally {
            // TestNG reuses threads, so the list must be removed, not just emptied.
            EXTRA_USERS.remove();
        }
    }
}