package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import ui.BookStorePage;

public class GuestAccessTest extends BaseTest {
    @Feature("Authentication")
    @Story("Guest access")
    @Test(
            description = "Unauthorized user sees Login button",
            groups = {"smoke", "ui"}
    )
    @Description("""
            Precondition: user is unauthorized (no session).
            UI: Book Store page is opened.
            Verification: Login button is visible on the page.
            """)
    public void unauthorizedUserSeesLoginButton() {

        new BookStorePage()
                .open()
                .shouldSeeLoginButton();
    }
}