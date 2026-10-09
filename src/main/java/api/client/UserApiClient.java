package api.client;

import api.model.CreateUserRequest;
import api.model.CreateUserResponse;
import api.model.GenerateTokenResponse;
import config.Config;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class UserApiClient {
    private final RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(Config.apiBaseUrl())
            .build();

    @Step("API: Create a test user {userName}")
    public CreateUserResponse createUser(String userName, String password) {
        Response response = given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(new CreateUserRequest(userName, password))
                .when()
                .post("/Account/v1/User")
                .then()
                .statusCode(201)
                .extract().response();

        return response.as(CreateUserResponse.class);
    }

    @Step("API: Generate an access token with full details (token + expires) for {userName}")
    public GenerateTokenResponse generateTokenDetails(String userName, String password) {
        Response response = given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .body(new CreateUserRequest(userName, password))
                .when()
                .post("/Account/v1/GenerateToken")
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.as(GenerateTokenResponse.class);
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
}
