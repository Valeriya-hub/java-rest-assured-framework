package ui;

import com.codeborne.selenide.SelenideElement;
import config.Config;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class BookStorePage extends BasePage<BookStorePage> {

    private final SelenideElement loginButton = $("#login");
    private final SelenideElement userNameLabel = $("#userName-value");

    @Step("Відкрити Book Store")
    public BookStorePage open() {
        return openPage(Config.bookStoreUrl());
    }

    @Step("Перевірити, що відкрита сторінка Book Store")
    public BookStorePage shouldBeOpened() {
        webdriver().shouldHave(urlContaining(Config.bookStoreUrl()));
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

    @Step("Натиснути на книгу '{title}'")
    public BookPage clickOnBook(String title) {
        $(By.linkText(title)).click();
        return new BookPage();
    }
}
