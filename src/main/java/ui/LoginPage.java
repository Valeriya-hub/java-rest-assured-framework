package ui;

import com.codeborne.selenide.SelenideElement;
import config.Config;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage<LoginPage> {
    private final SelenideElement usernameInput = $("#userName");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement loginButton = $("#login");

    @Step("Відкрити сторінку логіну")
    public LoginPage open() {
        return openPage(Config.loginUrl());
    }

    @Step("Ввести username: {username}")
    public LoginPage typeUsername(String username) {
        usernameInput.shouldBe(visible).setValue(username);
        return this;
    }

    @Step("Ввести пароль")
    public LoginPage typePassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Натиснути кнопку Login")
    public BookStorePage submitLogin() {
        loginButton.click();
        return new BookStorePage();
    }

    @Step("Залогінитись під користувачем {username}")
    public BookStorePage loginAs(String username, String password) {
        return typeUsername(username)
                .typePassword(password)
                .submitLogin();
    }
}
