package ui;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverConditions;
import config.Config;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static config.constants.UIConstants.BOOK_ADDED_ALERT;

public class BookPage extends BasePage<BookPage> {
    private final SelenideElement addToCollectionButton = $(".text-right #addNewRecordButton");
    private final SelenideElement bookIsbn = $("#ISBN-wrapper label#userName-value");
    private final SelenideElement bookTitle = $("#title-wrapper label#userName-value");
    private final SelenideElement bookAuthor = $("#author-wrapper label#userName-value");
    private final SelenideElement bookPublisher = $("#publisher-wrapper label#userName-value");
    private final SelenideElement bookDescription = $("#description-wrapper label#userName-value");

    @Step("Open book page with search parameter")
    public BookPage open(String searchQuery) {
        return openPage(Config.bookSearchUrl(searchQuery));
    }

    @Step("Verify that the page URL points to the book with ISBN {isbn}")
    public BookPage shouldHaveUrlForBook(String isbn) {
        webdriver().shouldHave(WebDriverConditions.url(Config.bookSearchUrl(isbn)));
        return this;
    }

    @Step("Add book to collection and confirm alert")
    public BookPage addBookToCollection() {
        addToCollectionButton.click();
        acceptBookAddedAlert();
        return this;
    }

    /**
     * The app reports the result with a native JS alert. It must be accepted explicitly:
     * confirm() waits for the alert, checks its text and clicks OK. Otherwise the alert
     * can pop up in the middle of the next command and break it.
     */
    @Step("Accept the browser alert about the added book")
    private void acceptBookAddedAlert() {
        confirm(BOOK_ADDED_ALERT);
    }

    @Step("Get book ISBN")
    public String getIsbn() {
        return bookIsbn.shouldBe(visible).getText();
    }

    @Step("Verify that the book title is '{expectedTitle}'")
    public BookPage shouldHaveTitle(String expectedTitle) {
        bookTitle.shouldHave(exactText(expectedTitle)
                .because("Book title on the details page should match the selected book"));
        return this;
    }


    @Step("Verify that the book author is '{expectedAuthor}'")
    public BookPage shouldHaveAuthor(String expectedAuthor) {
        bookAuthor.shouldHave(exactText(expectedAuthor)
                .because("Book author on the details page should match the selected book"));
        return this;
    }

    @Step("Verify that the book publisher is '{expectedPublisher}'")
    public BookPage shouldHavePublisher(String expectedPublisher) {
        bookPublisher.shouldHave(exactText(expectedPublisher)
                .because("Book publisher on the details page should match the selected book"));
        return this;
    }

    @Step("Verify that the book description is displayed and not empty")
    public BookPage shouldHaveDescription() {
        bookDescription.shouldBe(visible)
                .shouldNotBe(empty.because("Book description should not be empty"));
        return this;
    }

    @Step("Verify that the book ISBN is '{expectedIsbn}'")
    public void shouldHaveIsbn(String expectedIsbn) {
        bookIsbn.shouldHave(exactText(expectedIsbn)
                .because("Book ISBN on the details page should match the selected book"));
    }
}