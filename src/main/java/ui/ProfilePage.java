package ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import config.Config;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class ProfilePage extends BasePage<ProfilePage> {
    private final SelenideElement userNameLabel = $("#userName-value");
    private final SelenideElement goToBookStoreButton = $("#gotoStore");
    private final SelenideElement okButtonOnModal = $("#closeSmallModal-ok");
    private final SelenideElement logoutButton = $x("//button[text()='Logout']");

    private static final String BOOK_ROW_XPATH_TEMPLATE =
            "//div[@class='action-buttons']//a[contains(@href, '%s')]/ancestor::tr[1]";

    @Step("Відкрити сторінку профілю")
    public ProfilePage open() {
        return openPage(Config.profileUrl());
    }

    @Step("Перевірити, що відкрита сторінка профілю")
    public ProfilePage shouldBeOpened() {
        webdriver().shouldHave(urlContaining(Config.profileUrl()));
        return this;
    }

    @Step("Перевірити, що на сторінці профілю відображається ім'я {expectedUserName}")
    public ProfilePage shouldSeeUserName(String expectedUserName) {
        userNameLabel.shouldHave(exactText(expectedUserName).because("Ім'я користувача має збігатись з тим, під яким логінились"));
        return this;
    }

    @Step("Перейти на Book Store зі сторінки профілю")
    public BookStorePage clickGoToBookStoreButton() {
        goToBookStoreButton.click();
        return new BookStorePage();
    }

    private SelenideElement bookRow(String isbn) {
        String xpath = String.format(BOOK_ROW_XPATH_TEMPLATE, isbn);
        return $x(xpath);
    }

    @Step("Перевірити, що книга з ISBN {isbn} відображається в таблиці")
    public ProfilePage shouldBookVisible(String isbn) {
        bookRow(isbn).shouldBe(Condition.visible);
        return this;
    }

    @Step("Перевірити, що книги з ISBN {isbn} немає в таблиці")
    public ProfilePage shouldBookNotBeVisible(String isbn) {
        bookRow(isbn).shouldNot(Condition.exist);
        return this;
    }

    private SelenideElement deleteIconInRow(String isbn) {
        return $("#delete-record-" + isbn);
    }

    @Step("Видалити книгу з ISBN {isbn} через іконку Trash з підтвердженням")
    public ProfilePage deleteBook(String isbn) {
        deleteIconInRow(isbn).click();
        okButtonOnModal.shouldBe(Condition.visible).click();
        okButtonOnModal.shouldNotBe(visible);
        return this;
    }

    @Step("Перевірити, що видима кнопка Logout")
    public ProfilePage shouldSeeLogoutButton() {
        logoutButton.shouldBe(visible.because("Кнопка Logout має бути видима після успішного логіну"));
        return this;
    }
}
