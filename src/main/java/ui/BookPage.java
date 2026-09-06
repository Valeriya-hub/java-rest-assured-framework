package ui;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import config.Config;
import io.qameta.allure.Step;
import org.openqa.selenium.NoAlertPresentException;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class BookPage extends BasePage <BookPage> {
    private final SelenideElement addToCollectionButton = $(".text-right #addNewRecordButton");
    private final SelenideElement bookIsbn = $("#ISBN-wrapper label#userName-value");

    @Step("Відкрити сторінку книги з параметром пошуку")
    public BookPage open(String searchQuery) {
        return openPage(Config.bookSearchUrl(searchQuery));
    }

    @Step("Натиснути на кнопку 'Add to Collection'")
    public BookPage clickAddToCollectionButton() {
        addToCollectionButton.click();
        return this;
    }

    @Step("Якщо відображається JavaScript Alert натиснути ОК")
    public void dismissAlertIfPresent() {
        try {
            Selenide.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
        }
    }

        @Step("Отримати isbn книги")
        public String getIsbn () {
            return bookIsbn.shouldBe(visible).getText();
        }

    }