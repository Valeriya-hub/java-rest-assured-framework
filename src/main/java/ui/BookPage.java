package ui;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverConditions;
import config.Config;
import io.qameta.allure.Step;
import org.openqa.selenium.NoAlertPresentException;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;

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

    @Step("Click 'Add to Collection' button")
    public BookPage clickAddToCollectionButton() {
        addToCollectionButton.click();
        return this;
    }

    @Step("If JavaScript Alert is displayed, click OK")
    public void dismissAlertIfPresent() {
        try {
            Selenide.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
        }
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