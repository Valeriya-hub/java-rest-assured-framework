package ui;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import config.Config;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class BookStorePage extends BasePage<BookStorePage> {

    private final SelenideElement loginButton = $("#login");
    private final ElementsCollection rowCheckboxes = $$(".rt-tr-group input[type='checkbox']");
    private final SelenideElement userIcon = $("#userName-value");
    private final SelenideElement usernameLabel = $("#userName-value");
    private final SelenideElement gitPocketGuideBook = $("[id='see-book-Git Pocket Guide']");

    @Step("Відкрити Book Store")
    public BookStorePage open() {
        return openPage(Config.bookStoreUrl());
    }

    @Step("Перевірити, що видима кнопка Login (неавторизований стан)")
    public boolean isLoginButtonVisible() {
        return loginButton.shouldBe(visible).isDisplayed();
    }

    @Step("Перевірити, що видима іконка профілю (авторизований стан)")
    public boolean isProfileIconVisible() {
        return userIcon.shouldBe(visible, Duration.ofSeconds(10)).isDisplayed();
    }

    @Step("Отримати відображене ім'я користувача")
    public String getDisplayedUserName() {
        return usernameLabel.shouldBe(visible, Duration.ofSeconds(10)).getText();
    }

    @Step("Перевірити наявність чекбоксів для додавання книг у колекцію")
    public boolean areCollectionCheckboxesPresent() {
        return rowCheckboxes.size() > 0;
    }

    @Step("Натиснути на книгу")
    public BookPage clickOnBook() {
        gitPocketGuideBook.click();
        return new BookPage();
    }
}
