package tests;

import config.Config;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import ui.BookStorePage;

public class BookStoreTest extends BaseTest {
    private final BookStorePage bookStorePage = new BookStorePage();

    @Feature("Book Store UI")
    @Story("Books Page Layout & Controls")
    @Test(
            description = "UI-01: Перевірка відображення основних елементів сторінки Books",
            groups = {"ui", "smoke"}
    )
    @Description("""
            Перевірка URL, заголовка, таблиці книг, пошуку, пагінації та колонок (Title, Author, Publisher)
            """)
    public void shouldVerifyBooksPageInitialState() {

        bookStorePage.openPage(Config.bookStoreUrl())
                .verifyPageUrl()
                .verifyTabTitle("demosite")
                .verifyHeaderLogoVisible()
                .shouldSearchInputVisible()
                .shouldBooksTableVisible()
                .verifyEveryBookHasRequiredDetails()
                .verifyPaginationControlsVisible();
    }
}