package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import ui.BookStorePage;

public class GuestAccessTest extends BaseTest {
    @Feature("Автентифікація")
    @Story("Доступ гостя")
    @Test(
            description = "Неавторизований користувач бачить кнопку Login",
            groups = {"smoke", "ui"}
    )
    @Description("""
            Передумова: користувач неавторизований (сесія відсутня).
            UI: відкривається сторінка Book Store.
            Перевірка: кнопка Login є видимою на сторінці.
            """)
    public void unauthorizedUserSeesLoginButton() {

        new BookStorePage()
                .open()
                .shouldSeeLoginButton();
    }
}