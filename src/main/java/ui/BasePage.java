package ui;

import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.open;

public abstract class BasePage<T extends BasePage<T>> {
    @Step("Open page {0}")
    public T openPage(String url) {
        open(url);
        return (T) this;
    }
}
