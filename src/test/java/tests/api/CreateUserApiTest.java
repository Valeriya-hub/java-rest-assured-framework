package tests.api;

import api.model.CreateUserRequest;
import api.model.CreateUserResponse;
import config.constants.ApiErrors;
import data.UserFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.base.BaseApiTest;

import java.util.Map;

import static assertions.ApiAssertions.assertApiError;
import static assertions.ApiAssertions.assertValidUserId;
import static config.constants.ApiErrors.USER_NAME_AND_PASSWORD_REQUIRED;
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
        CreateUserRequest user = UserFactory.randomCredentials();

        Response response = userApiClient.sendCreateUserRequest(user);

        // Hard check first: if the user was not created, there is nothing to inspect or clean up.
        assertThat(response.statusCode())
                .as("Status code of the create user request")
                .isEqualTo(201);

        CreateUserResponse created = response.as(CreateUserResponse.class);

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
        CreateUserRequest user = UserFactory.randomCredentials();

        // Precondition: not the behaviour under test, so the asserting client method is used.
        CreateUserResponse firstUser = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(firstUser.userID(), user.userName(), user.password());

        Response duplicateResponse = userApiClient.sendCreateUserRequest(user);

        assertApiError(duplicateResponse, ApiErrors.USER_EXISTS);
    }

    @Feature("User account API")
    @Story("Create user - negative scenarios")
    @Test(
            description = "Create a user with empty username and password",
            groups = {"api", "regression"}
    )
    @Description("""
            Request: POST /Account/v1/User with empty username and password.
            Verification: HTTP 400 and the expected validation error.
            """)
    public void createUserWithEmptyCredentials() {
        Map<String, String> requestBody = Map.of(
                "userName", "",
                "password", ""
        );

        Response response =
                userApiClient.sendCreateUserRequestBody(requestBody);

        assertApiError(response, USER_NAME_AND_PASSWORD_REQUIRED);
    }

    @Feature("User account API")
    @Story("Create user - negative scenarios")
    @Test(
            description = "Create a user without username is rejected",
            groups = {"api", "regression"}
    )
    @Description("""
            Request: POST /Account/v1/User without the username field.
            Verification: HTTP 400 and the expected validation error.
            """)
    public void createUserWithoutUsername() {
        Map<String, String> requestBody = Map.of(
                "password", UserFactory.VALID_PASSWORD
        );

        Response response =
                userApiClient.sendCreateUserRequestBody(requestBody);

        assertApiError(response, USER_NAME_AND_PASSWORD_REQUIRED);
    }

    @Feature("User account API")
    @Story("Create user - negative scenarios")
    @Test(
            description = "Creating a user without password is rejected",
            groups = {"api", "regression"}
    )
    @Description("""
            Request: POST /Account/v1/User without the password field.
            Verification: HTTP 400 and the expected validation error.
            """)
    public void createUserWithoutPassword() {
        Map<String, String> requestBody = Map.of(
                "userName", "qa_user_" +
                        java.util.UUID.randomUUID()
                                .toString().substring(0, 8)
        );

        Response response =
                userApiClient.sendCreateUserRequestBody(requestBody);

        assertApiError(response, USER_NAME_AND_PASSWORD_REQUIRED);
    }
}