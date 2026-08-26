package ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.open;

public abstract class BasePage<T extends BasePage<T>> {
    @Step("Відкрити сторінку {0}")
    public T openPage(String url) {
        open(url);
        return (T) this;
    }

    protected void waitUntilVisible(SelenideElement element) {
        element.shouldBe(Condition.visible);
    }
}
