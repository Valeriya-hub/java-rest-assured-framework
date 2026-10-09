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
    // ISBN of a book from the public demoqa BookStore catalog ("Git Pocket Guide").
    // Fixed because the book catalog on demoqa is static and does not change between test runs.
    public static final String DEFAULT_ISBN = "9781449325862";
    private final RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(Config.apiBaseUrl())
            .build();

    @Step("API: Add book {isbn} to the collection of user {userId}")
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