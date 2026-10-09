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

    @Step("Open login page")
    public LoginPage open() {
        return openPage(Config.loginUrl());
    }

    @Step("Enter userName: {userName}")
    public LoginPage typeUserName(String userName) {
        userNameInput.shouldBe(visible).setValue(userName);
        return this;
    }

    @Step("Enter password")
    public LoginPage typePassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Click Login button")
    public ProfilePage submitLogin() {
        loginButton.click();
        return new ProfilePage();
    }

    @Step("Login as user {userName}")
    public ProfilePage loginAs(String userName, String password) {
        return typeUserName(userName)
                .typePassword(password)
                .submitLogin();
    }

    @Step("Attempt login as user {username}")
    public LoginPage attemptLoginAs(String userName, String password) {
        return typeUserName(userName)
                .typePassword(password)
                .clickLogin();
    }

    @Step("Click Login button")
    private LoginPage clickLogin() {
        loginButton.click();
        return this;
    }

    @Step("Verify invalid credentials error is displayed")
    public LoginPage shouldSeeInvalidCredentialsError() {
        errorMessageLabel.shouldHave(
                text(INVALID_CREDENTIALS)
                        .because("Appropriate message should be displayed for incorrect password")
        );
        return this;
    }

    @Step("Verify that user remains on login page")
    public void verifyIsStillOnLoginPage() {
        userNameInput.shouldBe(visible.because("User should remain on login page after failed attempt"));
    }
}
