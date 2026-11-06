package tests.api;

import api.client.BookApiClient;
import api.model.BookResponse;
import config.constants.ApiError;
import data.Book;
import data.BookPayloadProviders;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.base.BaseApiTest;

import static api.assertions.ApiAssertions.assertApiError;
import static api.assertions.ApiAssertions.assertJsonContentType;
import static api.assertions.BookAssertions.assertMissingIsbnParameterKnownDeviation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class GetBookApiTest extends BaseApiTest {
    private final BookApiClient bookApiClient = new BookApiClient();

    @Feature("Book store API")
    @Story("Get book by ISBN")
    @Test(
            description = "Get an existing book by ISBN",
            groups = {"api", "smoke"}
    )
    @Description("""
            Request: GET /BookStore/v1/Book?ISBN=... with the ISBN of a reference book
            (a public endpoint, no token is needed).
            Verification: status 200 and a JSON content type; isbn, title, author and publisher
            of the returned book match the reference data.
            """)
    public void getExistingBook() {
        Book expected = Book.DESIGNING_EVOLVABLE_WEB_APIS;
        Response response = bookApiClient.sendGetBookRequest(expected.isbn());

        // Hard checks first: the rest makes sense only for a successful, well-formed response.
        assertThat(response.statusCode())
                .as("Status code of the get book request")
                .isEqualTo(200);

        BookResponse actual = response.as(BookResponse.class);

        assertSoftly(softly -> {
            assertJsonContentType(softly, response);
            softly.assertThat(actual.isbn()).as("isbn").isEqualTo(expected.isbn());
            softly.assertThat(actual.title()).as("title").isEqualTo(expected.title());
            softly.assertThat(actual.author()).as("author").isEqualTo(expected.author());
            softly.assertThat(actual.publisher()).as("publisher").isEqualTo(expected.publisher());
        });
    }

    @Feature("Book store API")
    @Story("Get book by ISBN - negative scenarios")
    @Test(
            description = "Getting a book with an unavailable ISBN is rejected",
            dataProvider = "unavailableIsbns",
            dataProviderClass = BookPayloadProviders.class,
            groups = {"api", "regression"}
    )
    @Description("""
            Request: GET /BookStore/v1/Book?ISBN=... with a non-existent 13-digit ISBN,
            a malformed ISBN and an empty ISBN.
            Verification: the request is rejected with status 400 and the
            'ISBN supplied is not available in Books Collection!' error (code 1205).
            """)
    public void getBookWithUnavailableIsbn(String scenario, String isbn) {
        // 'scenario' is not used in the request: it only names the case in the test report.
        Response response = bookApiClient.sendGetBookRequest(isbn);

        assertApiError(response, ApiError.BOOK_ISBN_NOT_AVAILABLE);
    }

    @Feature("Book store API")
    @Story("Get book by ISBN - negative scenarios")
    @Test(
            description = "Getting a book without the ISBN parameter (known defect: 500)",
            groups = {"api", "regression"}
    )
    @Description("""
            Request: GET /BookStore/v1/Book without the ISBN query parameter.
            
            Known deviation: the server answers with 500 and an HTML error page instead of a 400
            JSON error (see README, Findings). The behaviour is pinned, so the test notices
            when it changes.
            """)
    public void getBookWithoutIsbnParameter() {
        Response response = bookApiClient.sendGetBookWithoutIsbnRequest();

        assertMissingIsbnParameterKnownDeviation(response);
    }
}
