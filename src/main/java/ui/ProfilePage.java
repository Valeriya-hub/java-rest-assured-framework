package ui;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import config.Config;
import io.qameta.allure.Step;
import org.openqa.selenium.Cookie;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class ProfilePage extends BasePage <ProfilePage>{
    private final SelenideElement userNameLabel = $("#userName-value");
    private final SelenideElement goToBookStoreButton = $("#gotoStore");

    @Step("Відкрити сторінку профілю")
    public ProfilePage open() {
        return openPage(Config.profileUrl());
    }

    @Step("Отримати відображене ім'я користувача на сторінці профілю")
    public String getDisplayedUserName() {
        return userNameLabel.shouldBe(visible).getText();
    }

    @Step("Перейти на Book Store зі сторінки профілю")
    public BookStorePage goToBookStore() {
        goToBookStoreButton.click();
        return new BookStorePage();
    }

    @Step("Отримати токен авторизації з cookie браузера")
    public String getAuthTokenFromCookie() {
        Cookie tokenCookie = Selenide.Wait()
                .withTimeout(Duration.ofSeconds(10))
                .until(driver -> WebDriverRunner.getWebDriver().manage().getCookieNamed("token"));
        return tokenCookie.getValue();
    }
}
