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
            description = "Verify main elements of Books page are displayed",
            groups = {"ui", "smoke"}
    )
    @Description("""
            Verification of URL, title, books table, search, pagination and columns (Title, Author, Publisher)
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