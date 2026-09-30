package api.client;

import api.model.AddBookRequest;
import config.Config;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;

import static io.restassured.RestAssured.given;

public class BookApiClient {
    // ISBN книги з публічного каталогу demoqa BookStore ("Git Pocket Guide").
    // Фіксований, бо каталог книг на demoqa статичний і не змінюється між прогонами.
    public static final String DEFAULT_ISBN = "9781449325862";
    private final RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(Config.apiBaseUrl())
            .build();

    @Step("API: додати книгу {isbn} в колекцію користувача {userId}")
    public void addBookToUser(String userId, String token, String isbn) {
        given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(new AddBookRequest(userId, List.of(new AddBookRequest.IsbnEntry(isbn))))
                .when()
                .post("/BookStore/v1/Books")
                .then()
                .statusCode(201);
    }
}