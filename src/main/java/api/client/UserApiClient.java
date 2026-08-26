package api.client;

import api.model.CreateUserRequest;
import api.model.CreateUserResponse;
import config.Config;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class UserApiClient {
    private final RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(Config.apiBaseUrl())
            .setContentType(ContentType.JSON)
//            .addFilter(new AllureRestAssured()) // логи запит/відповідь в Allure автоматично
            .build();

    @Step("API: створити тестового користувача {userName}")
    public CreateUserResponse createUser(String userName, String password) {
        Response response = given()
                .spec(spec)
                .body(new CreateUserRequest(userName, password))
                .when()
                .post("/Account/v1/User")
                .then()
                .statusCode(201)
                .extract().response();

        return response.as(CreateUserResponse.class);
    }

    @Step("API: видалити токен доступу (генерація токена для {userName})")
    public String generateToken(String userName, String password) {
        return given()
                .spec(spec)
                .body(new CreateUserRequest(userName, password))
                .when()
                .post("/Account/v1/GenerateToken")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    @Step("API: видалити тестового користувача {userId}")
    public void deleteUser(String userId, String token) {
        if (userId == null) {
            return; // нічого видаляти — юзер не створювався (кейс "неавторизований користувач")
        }
        given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/Account/v1/User/{userId}", userId)
                .then()
                // 204 — успішне видалення, 401 — токен вже невалідний/юзер вже видалений когось паралельно
                .statusCode(anyOf(is(204), is(401)));
    }

}
