package ui;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverConditions;
import config.Config;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class BookStorePage extends BasePage<BookStorePage> {

    private final SelenideElement headerLogo = $("header img");
    private final SelenideElement loginButton = $("#login");
    private final SelenideElement userNameLabel = $("#userName-value");
    private final SelenideElement searchInput = $(".form-control");
    private final SelenideElement tableBooksInput = $("table");

    private final ElementsCollection bookRows = $$("table tbody tr");
    private final ElementsCollection bookTitleLinks = $$("table tbody a");
    private final ElementsCollection tableHeaders = $$("table thead tr > *");
    private final ElementsCollection bookTitles = $$("table tbody tr td:nth-child(2) a");
    private final ElementsCollection bookAuthors = $$("table tbody tr td:nth-child(3)");
    private final ElementsCollection bookPublishers = $$("table tbody tr td:nth-child(4)");

    private final SelenideElement previousButton = $x("//button[text()='Previous']");
    private final SelenideElement nextButton = $x("//button[text()='Next']");
    private final SelenideElement pagePaginationCounter = $x("//span[contains(text(),'Page')]");

    @Step("Open Book Store")
    public BookStorePage open() {
        return openPage(Config.bookStoreUrl());
    }

    @Step("Verify that Book Store page is opened")
    public BookStorePage shouldBeOpened() {
        webdriver().shouldHave(urlContaining(Config.bookStoreUrl()));
        return this;
    }

    @Step("Verify that exact URL matches Config values")
    public BookStorePage verifyPageUrl() {
        webdriver().shouldHave(WebDriverConditions.url(Config.bookStoreUrl()));
        return this;
    }

    @Step("Verify browser tab title")
    public BookStorePage verifyTabTitle(String expectedTitle) {
        webdriver().shouldHave(WebDriverConditions.title(expectedTitle));
        return this;
    }

    @Step("Verify header logo visibility")
    public BookStorePage verifyHeaderLogoVisible() {
        headerLogo.shouldBe(visible
                .because("TOOLS QA logo in header should be visible"));
        return this;
    }

    @Step("Verify that Login button is visible (unauthorized state)")
    public void shouldSeeLoginButton() {
        loginButton.shouldBe(visible
                .because("Login button should be visible for unauthorized user"));
    }

    @Step("Verify that Login button is not visible (authorized state)")
    public void shouldNotSeeLoginButton() {
        loginButton.shouldNotBe(visible.because("Login button should not be visible for authorized user"));
    }

    @Step("Verify that user name {expectedUserName} is displayed on Book Store page")
    public BookStorePage shouldSeeUserName(String expectedUserName) {
        userNameLabel.shouldHave(exactText(expectedUserName).because("User name should match the one used for login"));
        return this;
    }

    @Step("Verify that search input is visible")
    public BookStorePage shouldSearchInputVisible() {
        searchInput.shouldBe(visible.because("Search input must be visible"));
        return this;
    }

    @Step("Verify that books table is visible")
    public BookStorePage shouldBooksTableVisible() {
        tableBooksInput.shouldBe(visible.because("Books table must be visible"));
        return this;
    }

    @Step("Verify that each book in the table has Title, Author and Publisher")
    public BookStorePage verifyEveryBookHasRequiredDetails() {

        tableHeaders.shouldHave(exactTexts("Image", "Title", "Author", "Publisher")
                .because("Table columns must be in order Image, Title, Author, Publisher — "
                        + "cell positional locators depend on this"));

        bookRows.shouldHave(sizeGreaterThan(0)
                .because("At least one book should be displayed in the table"));
        int booksCount = bookRows.size();

        // Each row has all three cells (count matches row count).
        bookTitles.shouldHave(size(booksCount)
                .because("Each of " + booksCount + " rows must contain Title (book link)"));
        bookAuthors.shouldHave(size(booksCount)
                .because("Each of " + booksCount + " rows must contain Author cell"));
        bookPublishers.shouldHave(size(booksCount)
                .because("Each of " + booksCount + " rows must contain Publisher cell"));

        // None of the cells are empty.
        shouldAllBeNonBlank(bookTitles, "Title");
        shouldAllBeNonBlank(bookAuthors, "Author");
        shouldAllBeNonBlank(bookPublishers, "Publisher");
        return this;
    }

    private void shouldAllBeNonBlank(ElementsCollection column, String columnName) {
        column.shouldHave(allMatch(columnName + " is not blank",
                cell -> !cell.getText().isBlank())
                .because("Each book in the table must have a non-blank " + columnName));
    }

    @Step("Click on book '{title}'")
    public BookPage clickOnBook(String title) {
        $(By.linkText(title)).click();
        return new BookPage();
    }

    @Step("Verify pagination controls (Previous, Next and 'Page 1 of 1')")
    public void verifyPaginationControlsVisible() {

        previousButton
                .shouldBe(visible.because("'Previous' button should be visible"))
                .shouldBe(disabled.because("'Previous' button should be disabled when first page is opened"));

        nextButton
                .shouldBe(visible.because("'Next' button should be visible"))
                .shouldBe(disabled.because("'Next' button should be disabled when there are no next pages"));

        pagePaginationCounter
                .shouldBe(visible.because("Page indicator should be visible"))
                .shouldHave(exactText("Page 1 of 1").because("Pagination text should match initial state 'Page 1 of 1'"));
    }

    @Step("Search for a book by its full title")
    public BookStorePage searchBookByTitle(String bookTitle) {
        searchInput.shouldBe(visible).setValue(bookTitle);
        return this;
    }

    @Step("Verify that only book with {0} title is displayed in the table")
    public BookStorePage shouldShowOnlyBook(String bookTitle) {
        bookRows.shouldHave(size(1)
                .because("Only one book should be displayed in the table"));
        bookTitleLinks.shouldHave(exactTexts(bookTitle)
                .because("Only the book with the " + bookTitle + "title will be displayed in the table."));
        return this;
    }

    @Step("Verify that book with {0} title is not displayed in the table")
    public void shouldNotShowBook(String bookTitle) {
        $(By.linkText(bookTitle)).shouldNot(exist
                .because("The book with the " + bookTitle + " title should not be displayed in the table."));
    }

    @Step("Verify that each displayed book contains '{text}' and the result is not empty.")
    public BookStorePage shouldShowOnlyBooksContaining(String text) {
        String expected = text.toLowerCase();
        bookRows.shouldHave(allMatch("each line contains '" + text + "'",
                row -> row.getText().toLowerCase().contains(expected))
                .because("Every displayed book in the search results must contain text '" + text + "'"));
        bookRows.shouldHave(sizeGreaterThan(0)
                .because("Search result table should contain at least one book for query '" + text + "'"));
        return this;
    }

    @Step("Verify that the book '{title}' is displayed in the table.")
    public BookStorePage shouldShowBook(String title) {
        $(By.linkText(title)).shouldBe(visible);
        return this;
    }

    @Step("Verify that multiple books with '{partialTitle}' in their titles are displayed.")
    public void shouldShowSeveralBooksWithTitleContaining(String partialTitle) {
        String expected = partialTitle.toLowerCase();

        // First, we wait for the filter to be applied: each displayed name contains the text.
        bookTitles.shouldHave(allMatch("title contains '" + partialTitle + "'",
                title -> title.getText().toLowerCase().contains(expected))
                .because("Each displayed book must contain '" + partialTitle + "' in the title"));

        // And only then do we verify that there are several books (rather than one or none).
        bookTitles.shouldHave(sizeGreaterThan(1)
                .because("Search by part of the title '" + partialTitle + "' has to return a few books"));
    }

    @Step("Verify that no books are displayed.")
    public void shouldShowNoBooks() {
        bookTitleLinks.shouldHave(size(0));
    }
}
