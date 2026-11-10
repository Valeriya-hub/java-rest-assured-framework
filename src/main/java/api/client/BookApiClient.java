package api.client;

import api.model.AddBookRequest;
import api.model.DeleteBookRequest;
import config.Config;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;

import static io.restassured.RestAssured.given;

public class BookApiClient {
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

    @Step("API: Send get all books request")
    public Response sendGetBooksRequest() {
        return given()
                .spec(spec)
                .when()
                .get("/BookStore/v1/Books");
    }

    @Step("API: Send get book request, ISBN = {isbn}")
    public Response sendGetBookRequest(String isbn) {
        return given()
                .spec(spec)
                .queryParam("ISBN", isbn)
                .when()
                .get("/BookStore/v1/Book");
    }

    @Step("API: Send get book request without the ISBN parameter")
    public Response sendGetBookWithoutIsbnRequest() {
        return given()
                .spec(spec)
                .when()
                .get("/BookStore/v1/Book");
    }

    @Step("API: Send add book request")
    public Response sendAddBookRequest(String token, Object body) {
        RequestSpecification request = given()
                .spec(spec)
                .contentType(ContentType.JSON);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request
                .body(body)
                .when()
                .post("/BookStore/v1/Books");
    }

    @Step("API: Send delete book request")
    public Response sendDeleteBookRequest(String token, DeleteBookRequest body) {
        return authorizedJson(token)
                .body(body)
                .when()
                .delete("/BookStore/v1/Book");
    }

    @Step("API: Send delete book request")
    public Response sendDeleteBookRequest(String token, Object body) {
        return authorizedJson(token)
                .body(body)
                .when()
                .delete("/BookStore/v1/Book");
    }

    /**
     * Precondition-style call: expects success (204). Use it when deleting a book
     * is only test setup, not the behaviour under test.
     */
    @Step("API: Delete book {isbn} from the collection of user {userId}")
    public void deleteBookFromUser(String userId, String token, String isbn) {
        sendDeleteBookRequest(token, new DeleteBookRequest(userId, isbn))
                .then()
                .statusCode(204);
    }

    // A null token means "no Authorization header" (negative scenarios).
    private RequestSpecification authorizedJson(String token) {
        RequestSpecification request = given()
                .spec(spec)
                .contentType(ContentType.JSON);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request;
    }
}