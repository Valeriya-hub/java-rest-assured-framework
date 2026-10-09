package ui;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import config.Config;
import io.qameta.allure.Step;
import org.openqa.selenium.NoAlertPresentException;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class BookPage extends BasePage<BookPage> {
    private final SelenideElement addToCollectionButton = $(".text-right #addNewRecordButton");
    private final SelenideElement bookIsbn = $("#ISBN-wrapper label#userName-value");

    @Step("Open book page with search parameter")
    public BookPage open(String searchQuery) {
        return openPage(Config.bookSearchUrl(searchQuery));
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
}