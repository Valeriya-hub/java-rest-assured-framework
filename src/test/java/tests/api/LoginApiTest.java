package tests.api;

import api.model.CredentialsRequest;
import api.model.CredentialsResponse;
import api.model.LoginResponse;
import config.constants.ApiError;
import data.UserFactory;
import data.UserPayloadProviders;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.base.BaseApiTest;
import tests.base.KnownIssue;

import java.util.Map;

import static api.assertions.ApiAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class LoginApiTest extends BaseApiTest {

    @Feature("User account API")
    @Story("Login")
    @Test(
            description = "Login with valid credentials",
            groups = {"api", "smoke"}
    )
    @Description("""
            Precondition: a user is created via API.
            Request: POST /Account/v1/Login with the user's credentials.
            Verification: status 200; the body contains the userId of the created user,
            the username that was sent, a token and an expiration date in the future.
            """)
    public void loginWithValidCredentials() {
        CredentialsRequest user = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        Response response = userApiClient.sendLoginRequest(user);

        // Hard check first: without a successful response there is no body worth inspecting.
        assertThat(response.statusCode())
                .as("Status code of the login request")
                .isEqualTo(200);

        // No token update for cleanup: Login returns the existing token (the same one
        // GenerateToken issued), so the token registered for cleanup stays valid.
        LoginResponse body = response.as(LoginResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(body.userId())
                    .as("userId")
                    .isEqualTo(created.userID());
            softly.assertThat(body.username())
                    .as("username")
                    .isEqualTo(user.userName());
            softly.assertThat(body.token())
                    .as("token")
                    .isNotBlank();
            assertExpiresInFuture(softly, body.expires());
        });
    }

    @Feature("User account API")
    @Story("Login - security")
    @Test(
            description = "Login response does not expose the user's password (KNOWN ISSUE)",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API.
            Request: POST /Account/v1/Login with the user's credentials.
            Verification (desired behaviour): the response body does not contain the user's password.
            
            Known issue: the application returns the password in plain text (README, Findings #1).
            Until it is fixed the test is reported as skipped; when it is fixed,
            the test fails to remind that the marker must be removed.
            """)
    public void loginResponseDoesNotExposePassword() {
        CredentialsRequest user = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        Response response = userApiClient.sendLoginRequest(user);

        // Not part of the known issue: without a successful login there is nothing to inspect.
        assertThat(response.statusCode())
                .as("Status code of the login request")
                .isEqualTo(200);

        // A boolean is asserted on purpose: asserting on the body itself would print
        // the password and the token in the failure message and in the report.
        boolean exposesPassword = response.asString().contains(user.password());

        KnownIssue.expectFailure("README, Findings #1", () ->
                assertThat(exposesPassword)
                        .as("Login response body must not contain the user's password")
                        .isFalse());
    }

    // Authentication failures: the body is valid, but the credentials do not match any user.
    // The application reports a failed login with HTTP 200 and an empty body (known deviation,
    // see README, Findings).

    @Feature("User account API")
    @Story("Login - negative scenarios")
    @Test(
            description = "Login with an invalid password does not authenticate the user",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API.
            Request: POST /Account/v1/Login with the correct username and a wrong password.
             Verification: no authentication data is returned (empty body).
            
            Known deviation: the application answers 200 instead of an error status (README, Findings).
            """)
    public void loginWithInvalidPassword() {
        CredentialsRequest user = UserFactory.randomCredentials();
        CredentialsResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        Response response = userApiClient.sendLoginRequest(
                new CredentialsRequest(user.userName(), UserFactory.WRONG_PASSWORD));

        assertLoginNotAuthenticated(response);
    }

    @Feature("User account API")
    @Story("Login - negative scenarios")
    @Test(
            description = "Login with a non-existing user does not authenticate anyone",
            groups = {"api", "regression"}
    )
    @Description("""
            Request: POST /Account/v1/Login with credentials of a user that was never created.
            Verification: no authentication data is returned (empty body).
            
            Known deviation: the application answers 200 instead of an error status (README, Findings).
            """)
    public void loginWithNonExistingUser() {
        // Unique credentials that were never registered: no precondition and no cleanup needed.
        CredentialsRequest unknownUser = UserFactory.randomCredentials();

        Response response = userApiClient.sendLoginRequest(unknownUser);

        assertLoginNotAuthenticated(response);
    }

    @Feature("User account API")
    @Story("Login - negative scenarios")
    @Test(
            description = "Login with an empty field value does not authenticate the user",
            dataProvider = "emptyValuePayloads",
            dataProviderClass = UserPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Request: POST /Account/v1/Login with an empty string as userName and/or password.
            Verification: no authentication data is returned (empty body).
            
            Known deviation: unlike an absent field, an empty value is not rejected with a
            validation error; the application answers 200 (README, Findings).
            """)
    public void loginWithEmptyValue(String scenario, Map<String, String> body) {
        // 'scenario' is not used in the body: it only names the case in the test report.
        Response response = userApiClient.sendLoginRequest(body);

        assertLoginNotAuthenticated(response);
    }

    // Input validation: the body itself is invalid. No user is needed, so no cleanup either.

    @Feature("User account API")
    @Story("Login - negative scenarios")
    @Test(
            description = "Login without a required field is rejected with a validation error (KNOWN ISSUE)",
            dataProvider = "missingFieldPayloads",
            dataProviderClass = UserPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Request: POST /Account/v1/Login with userName and/or password absent from the JSON body.
            Verification (desired behaviour): the input is handled gracefully (no server error,
            no internal details in the body) and rejected with the validation error.
            
            Known issue: the application crashes with HTTP 500 and returns a stack trace
            (README, Findings #5). Until it is fixed the test is reported as skipped;
            when the behaviour is fixed, the test fails to remind that the marker must be removed.
            """)
    public void loginWithMissingField(String scenario, Map<String, String> body) {
        // 'scenario' is not used in the body: it only names the case in the test report.
        Response response = userApiClient.sendLoginRequest(body);

        KnownIssue.expectFailure("README, Findings #5", () -> {
            assertHandledGracefully(response);
            assertApiError(response, ApiError.USER_NAME_AND_PASSWORD_REQUIRED);
        });
    }
}
