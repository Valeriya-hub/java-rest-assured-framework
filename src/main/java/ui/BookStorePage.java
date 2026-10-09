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
    private final ElementsCollection tableHeaders = $$("table thead tr > *");
    private final ElementsCollection bookTitles = $$("table tbody tr td:nth-child(2) a");
    private final ElementsCollection bookAuthors = $$("table tbody tr td:nth-child(3)");
    private final ElementsCollection bookPublishers = $$("table tbody tr td:nth-child(4)");

    private final SelenideElement previousButton = $x("//button[text()='Previous']");
    private final SelenideElement nextButton = $x("//button[text()='Next']");
    private final SelenideElement pagePaginationCounter = $x("//span[contains(text(),'Page')]");

    @Step("Відкрити Book Store")
    public BookStorePage open() {
        return openPage(Config.bookStoreUrl());
    }

    @Step("Перевірити, що відкрита сторінка Book Store")
    public BookStorePage shouldBeOpened() {
        webdriver().shouldHave(urlContaining(Config.bookStoreUrl()));
        return this;
    }

    @Step("Перевірити, що точний URL відповідає значенням з Config")
    public BookStorePage verifyPageUrl() {
        webdriver().shouldHave(WebDriverConditions.url(Config.bookStoreUrl()));
        return this;
    }

    @Step("Перевірити назву вкладки браузера")
    public BookStorePage verifyTabTitle(String expectedTitle) {
        webdriver().shouldHave(WebDriverConditions.title(expectedTitle));
        return this;
    }

    @Step("Перевірити видимість логотипа у шапці сторінки")
    public BookStorePage verifyHeaderLogoVisible() {
        headerLogo.shouldBe(visible.because("Логотип TOOLS QA у шапці має бути видимим"));
        return this;
    }

    @Step("Перевірити, що видима кнопка Login (неавторизований стан)")
    public BookStorePage shouldSeeLoginButton() {
        loginButton.shouldBe(visible.because("Кнопка Login має бути видима для неавторизованого користувача"));
        return this;
    }

    @Step("Перевірити, що не видима кнопка Login (авторизований стан)")
    public BookStorePage shouldNotSeeLoginButton() {
        loginButton.shouldNotBe(visible.because("Кнопка Login має бути не видима для авторизованого користувача"));
        return this;
    }

    @Step("Перевірити, що на сторінці Book Store відображається ім'я {expectedUserName}")
    public BookStorePage shouldSeeUserName(String expectedUserName) {
        userNameLabel.shouldHave(exactText(expectedUserName).because("Ім'я користувача має збігатись з тим, під яким логінились"));
        return this;
    }

    @Step("Перевірити, що видиме поле пошуку")
    public BookStorePage shouldSearchInputVisible() {
        searchInput.shouldBe(visible.because("Поле пошуку мусить бути видимим"));
        return this;
    }

    @Step("Перевірити, що видима таблиця книг")
    public BookStorePage shouldBooksTableVisible() {
        tableBooksInput.shouldBe(visible.because("Таблиця книг мусить бути видимима"));
        return this;
    }

    @Step("Перевірити, що кожна книга в таблиці має Title, Author та Publisher")
    public BookStorePage verifyEveryBookHasRequiredDetails() {

        tableHeaders.shouldHave(exactTexts("Image", "Title", "Author", "Publisher")
                .because("Колонки таблиці мають іти в порядку Image, Title, Author, Publisher — "
                        + "від цього залежать позиційні локатори комірок"));

        bookRows.shouldHave(sizeGreaterThan(0)
                .because("У таблиці має відображатися хоча б одна книга"));
        int booksCount = bookRows.size();

        // У кожного рядка є всі три комірки (кількість збігається з кількістю рядків).
        bookTitles.shouldHave(size(booksCount)
                .because("Кожен із " + booksCount + " рядків має містити Title (посилання на книгу)"));
        bookAuthors.shouldHave(size(booksCount)
                .because("Кожен із " + booksCount + " рядків має містити комірку Author"));
        bookPublishers.shouldHave(size(booksCount)
                .because("Кожен із " + booksCount + " рядків має містити комірку Publisher"));

        // Жодна з комірок не порожня.
        shouldAllBeNonBlank(bookTitles, "Title");
        shouldAllBeNonBlank(bookAuthors, "Author");
        shouldAllBeNonBlank(bookPublishers, "Publisher");
        return this;
    }

    private void shouldAllBeNonBlank(ElementsCollection column, String columnName) {
        column.shouldHave(allMatch(columnName + " не порожній",
                cell -> !cell.getText().isBlank())
                .because("Кожна книга в таблиці має мати непорожнє значення " + columnName));
    }

    @Step("Натиснути на книгу '{title}'")
    public BookPage clickOnBook(String title) {
        $(By.linkText(title)).click();
        return new BookPage();
    }

    @Step("Перевірити контролери пагінації (Previous, Next та 'Page 1 of 1')")
    public BookStorePage verifyPaginationControlsVisible() {

        previousButton
                .shouldBe(visible.because("Кнопка 'Previous' має бути видимою"))
                .shouldBe(disabled.because("Кнопка 'Previous' має бути неактивною, коли відкрита перша сторінка"));

        nextButton
                .shouldBe(visible.because("Кнопка 'Next' має бути видимою"))
                .shouldBe(disabled.because("Кнопка 'Next' має бути неактивною, коли немає наступних сторінок"));

        pagePaginationCounter
                .shouldBe(visible.because("Індикатор сторінки має бути видимим"))
                .shouldHave(exactText("Page 1 of 1").because("Текст пагінації має відповідати початковому стану 'Page 1 of 1'"));

        return this;
    }
}
