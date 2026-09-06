package api.client;

import api.model.CreateUserRequest;
import api.model.CreateUserResponse;
import config.Config;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
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
            .addFilter(new AllureRestAssured())
            .build();
    private String userId;
    private String token;

    public UserApiClient() {
    }

    public UserApiClient(String id, String authToken) {
        this.userId = id;
        this.token = authToken;
    }

    @Step("API: створити тестового користувача {userName}")
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

    @Step("API: генерація токена доступу для {userName}")
    public String generateToken(String userName, String password) {
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

        return response.path("token");
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
                // 204 — успішне видалення, 401 — токен вже невалідний/юзер вже видалений
                .statusCode(anyOf(is(204), is(401)));
    }

    @Step("API: отримати список книг користувача")
    public List<String> getUserBookIsbns() {
        // ВАЖЛИВО: demoqa інвалідує попередній токен при новому логіні (UI чи API),
        // тому тут має передаватись АКТУАЛЬНИЙ токен — той, що активний на момент виклику.
        return
                given()
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
