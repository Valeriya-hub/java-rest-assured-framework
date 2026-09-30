package ui;

import com.codeborne.selenide.SelenideElement;
import config.Config;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static config.constants.UIErrorMessages.INVALID_CREDENTIALS;

public class LoginPage extends BasePage<LoginPage> {
    private final SelenideElement userNameInput = $("#userName");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement loginButton = $("#login");
    private final SelenideElement errorMessageLabel = $("#name");

    @Step("Відкрити сторінку логіну")
    public LoginPage open() {
        return openPage(Config.loginUrl());
    }

    @Step("Ввести userName: {userName}")
    public LoginPage typeUserName(String userName) {
        userNameInput.shouldBe(visible).setValue(userName);
        return this;
    }

    @Step("Ввести пароль")
    public LoginPage typePassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Натиснути кнопку Login")
    public ProfilePage submitLogin() {
        loginButton.click();
        return new ProfilePage();
    }

    @Step("Залогінитись під користувачем {userName}")
    public ProfilePage loginAs(String userName, String password) {
        return typeUserName(userName)
                .typePassword(password)
                .submitLogin();
    }

    @Step("Спроба логіну під користувачем {username}")
    public LoginPage attemptLoginAs(String userName, String password) {
        return typeUserName(userName)
                .typePassword(password)
                .clickLogin();
    }

    @Step("Натиснути кнопку Login")
    private LoginPage clickLogin() {
        loginButton.click();
        return this;
    }

    @Step("Перевірити відображення помилки про невірні облікові дані")
    public LoginPage shouldSeeInvalidCredentialsError() {
        errorMessageLabel.shouldHave(
                text(INVALID_CREDENTIALS)
                        .because("При невірному паролі має відображатися відповідне повідомлення")
        );
        return this;
    }

    @Step("Перевірити, що користувач залишається на сторінці логіну")
    public void verifyIsStillOnLoginPage() {
        userNameInput.shouldBe(visible.because("Користувач має лишитись на сторінці логіну після невдалої спроби"));
    }
}
