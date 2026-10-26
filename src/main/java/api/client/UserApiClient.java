package api.client;

import api.model.CredentialsRequest;
import api.model.CredentialsResponse;
import api.model.GenerateTokenResponse;
import config.Config;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class UserApiClient {
    private final RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(Config.apiBaseUrl())
            .build();

    /**
     * Raw call: sends the request and returns the response WITHOUT any assertions.
     * Use it in API tests that verify status codes and bodies (including negative scenarios).
     */
    @Step("API: Send create user request")
    public Response sendCreateUserRequest(CredentialsRequest request) {
        return postJson("/Account/v1/User", request);
    }

    /**
     * Raw call with an arbitrary JSON body. Needed for negative tests where the body
     * cannot be expressed with CreateUserRequest, e.g. a field is missing altogether
     * (a record would send it as null instead of omitting it).
     */
    @Step("API: Send create user request with a custom body")
    public Response sendCreateUserRequest(Map<String, ?> body) {
        return postJson("/Account/v1/User", body);
    }

    /**
     * Precondition-style call: expects success (201) and returns the parsed body.
     * Use it when creating a user is only test setup, not the behaviour under test.
     */
    @Step("API: Create a test user {userName}")
    public CredentialsResponse createUser(String userName, String password) {

        return sendCreateUserRequest(new CredentialsRequest(userName, password))
                .then()
                .statusCode(201)
                .extract()
                .as(CredentialsResponse.class);
    }

    /**
     * Raw call: sends the generate token request and returns the response WITHOUT any assertions.
     */
    @Step("API: Send generate token request")
    public Response sendGenerateTokenRequest(CredentialsRequest credentials) {
        return postJson("/Account/v1/GenerateToken", credentials);
    }

    @Step("API: Generate an access token with full details (token + expires) for {userName}")
    public GenerateTokenResponse generateTokenDetails(String userName, String password) {
        return sendGenerateTokenRequest(new CredentialsRequest(userName, password))
                .then()
                .statusCode(200)
                .extract()
                .as(GenerateTokenResponse.class);
    }

    @Step("API: Send login request")
    public Response sendLoginRequest(CredentialsRequest credentials) {
        return postJson("/Account/v1/Login", credentials);
    }

    @Step("API: Send login request with a custom body")
    public Response sendLoginRequest(Map<String, ?> body) {
        return given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/Account/v1/Login");
    }

    @Step("API: Delete the test user {userId}")
    public void deleteUser(String userId, String token) {
        if (userId == null) {
            return; // nothing to delete — user was not created ("unauthorized user" case)
        }
        given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/Account/v1/User/{userId}", userId)
                .then()
                // 204 — successful deletion, 401 — token already invalid/user already deleted
                .statusCode(anyOf(is(204), is(401)));
    }

    @Step("API: Retrieve the user's book collection")
    public List<String> getUserBookIsbns(String userId, String token) {
        return given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .cookie("token", token)
                .when()
                .get("/Account/v1/User/{userId}", userId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("books.isbn", String.class);
    }

    @Step("API: Send get user request for {userId}")
    public Response sendGetUserRequest(String userID, String token) {
        return given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .cookie("token", token)
                .when()
                .get("/Account/v1/User/{userId}", userID);
    }

    private Response postJson(String path, Object body) {
        return given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);
    }
}
