package tests.api;

import api.model.CredentialsRequest;
import api.model.CredentialsResponse;
import config.constants.ApiError;
import data.UserFactory;
import data.UserPayloadProviders;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.base.BaseApiTest;

import java.util.Map;

import static api.assertions.ApiAssertions.assertApiError;
import static api.assertions.ApiAssertions.assertValidUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CreateUserApiTest extends BaseApiTest {

    @Feature("User account API")
    @Story("Create user")
    @Test(
            description = "Create a user with valid credentials",
            groups = {"api", "smoke"}
    )
    @Description("""
            Request: POST /Account/v1/User with a unique username and a valid password.
            Verification: status 201; the body contains a userID in UUID format
            and the username that was sent. The userID is stored for cleanup.
            """)
    public void createUserWithValidCredentials() {
        CredentialsRequest user = UserFactory.randomCredentials();

        Response response = userApiClient.sendCreateUserRequest(user);

        // Hard check first: if the user was not created, there is nothing to inspect or clean up.
        assertThat(response.statusCode())
                .as("Status code of the create user request")
                .isEqualTo(201);

        CredentialsResponse created = response.as(CredentialsResponse.class);

        // Stores userID (testUserId) and registers the user for deletion in tearDown.
        // Done BEFORE the remaining checks, so a failing check does not leave the user behind.
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        assertSoftly(softly -> {
            assertValidUserId(softly, created.userID());
            softly.assertThat(created.username())
                    .as("username of the created user")
                    .isEqualTo(user.userName());
        });
    }

    @Feature("User account API")
    @Story("Create user - negative scenarios")
    @Test(
            description = "Creating a user with an already taken username is rejected",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API.
            Request: POST /Account/v1/User again with the same username.
            Verification: the request is rejected with the 'User exists' error
            (status, error code and message).
            """)
    public void createUserWithDuplicateUsername() {
        CredentialsRequest user = UserFactory.randomCredentials();

        // Precondition: not the behaviour under test, so the asserting client method is used.
        CredentialsResponse firstUser = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(firstUser.userID(), user.userName(), user.password());

        Response duplicateResponse = userApiClient.sendCreateUserRequest(user);

        assertApiError(duplicateResponse, ApiError.USER_EXISTS);
    }

    // No cleanup in the two tests below: a user must not be created from an invalid body.

    @Feature("User account API")
    @Story("Create user - negative scenarios")
    @Test(
            description = "Creating a user without a required field is rejected",
            dataProvider = "missingFieldPayloads",
            dataProviderClass = UserPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Request: POST /Account/v1/User with userName and/or password absent from the JSON body.
            Verification: the request is rejected with status 400 and the
            'UserName and Password required' error.
            """)
    public void createUserWithMissingField(String scenario, Map<String, String> body) {
        // 'scenario' is not used in the body: it only names the case in the test report.
        Response response = userApiClient.sendCreateUserRequest(body);

        assertApiError(response, ApiError.USER_NAME_AND_PASSWORD_REQUIRED);

    }

    @Feature("User account API")
    @Story("Create user - negative scenarios")
    @Test(
            description = "Creating a user with an empty field value is rejected",
            dataProvider = "emptyValuePayloads",
            dataProviderClass = UserPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Request: POST /Account/v1/User with an empty string as userName and/or password.
            Verification: the request is rejected with status 400 and the
            'UserName and Password required' error.
            """)
    public void createUserWithEmptyValue(String scenario, Map<String, String> body) {
        // 'scenario' is not used in the body: it only names the case in the test report.
        Response response = userApiClient.sendCreateUserRequest(body);

        assertApiError(response, ApiError.USER_NAME_AND_PASSWORD_REQUIRED);
    }
}