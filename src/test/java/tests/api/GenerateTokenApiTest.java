package tests.api;

import api.model.CreateUserRequest;
import api.model.CreateUserResponse;
import api.model.GenerateTokenResponse;
import config.constants.AuthResult;
import data.UserFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import tests.base.BaseApiTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class GenerateTokenApiTest extends BaseApiTest {
    @Feature("User account API")
    @Story("Generate token")
    @Test(
            description = "Generate a token with valid credentials",
            groups = {"api", "smoke"}
    )
    @Description("""
            Precondition: a user is created via API.
            Request: POST /Account/v1/GenerateToken with the user's credentials.
            Verification: status 200; the body contains a token, status 'Success'
            and the result 'User authorized successfully.'.
            """)
    public void generateTokenWithValidCredentials() {
        CreateUserRequest user = UserFactory.randomCredentials();
        CreateUserResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        Response response = userApiClient.sendGenerateTokenRequest(user);

        // Hard check first: without a successful response there is no body worth inspecting.
        assertThat(response.statusCode())
                .as("Status code of the generate token request")
                .isEqualTo(200);

        GenerateTokenResponse body = response.as(GenerateTokenResponse.class);

        // demoqa may invalidate the previous token when a new one is generated,
        // so cleanup in tearDown must use the token from this response.
        if (body.token() != null) {
            updateTestUserToken(body.token(), body.expires());
        }

        assertSoftly(softly -> {
            softly.assertThat(body.token())
                    .as("token")
                    .isNotBlank();
            softly.assertThat(body.status())
                    .as("status")
                    .isEqualTo(AuthResult.AUTHORIZED.status());
            softly.assertThat(body.result())
                    .as("result")
                    .isEqualTo(AuthResult.AUTHORIZED.result());
        });
    }

    @Feature("User account API")
    @Story("Generate token")
    @Test(
            description = "Generating a token with an invalid password fails",
            groups = {"api", "regression"}
    )
    @Description("""
            Precondition: a user is created via API.
            Request: POST /Account/v1/GenerateToken with the correct username and a wrong password.
            Verification: authorization fails: status 'Failed', result 'User authorization failed.',
            and no token is issued.
            """)
    public void generateTokenWithInvalidPassword() {
        CreateUserRequest user = UserFactory.randomCredentials();
        CreateUserResponse created = userApiClient.createUser(user.userName(), user.password());
        registerCreatedUserForCleanup(created.userID(), user.userName(), user.password());

        Response response = userApiClient.sendGenerateTokenRequest(
                new CreateUserRequest(user.userName(), UserFactory.WRONG_PASSWORD));
        GenerateTokenResponse body = response.as(GenerateTokenResponse.class);

        assertSoftly(softly -> {
            // demoqa reports a failed authorization in the body, not with an HTTP error code.
            softly.assertThat(response.statusCode())
                    .as("HTTP status code")
                    .isEqualTo(200);
            softly.assertThat(body.status())
                    .as("status")
                    .isEqualTo(AuthResult.AUTHORIZATION_FAILED.status());
            softly.assertThat(body.result())
                    .as("result")
                    .isEqualTo(AuthResult.AUTHORIZATION_FAILED.result());
            softly.assertThat(body.token())
                    .as("token must not be issued for a failed authorization")
                    .isNull();
        });
    }
}