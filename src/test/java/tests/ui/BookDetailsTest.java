package tests.ui;

import data.Book;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import ui.BookStorePage;

public class BookDetailsTest extends BaseTest {

    @Feature("Book Store UI")
    @Story("Book Details Navigation")
    @Test(
            description = "Verify book details page content and URL",
            groups = {"ui", "regression"}
    )
    @Description("""
            Preconditions: User is not logged in; the Book Store page is open.
            UI: Click on the book in the table.
            Verification: The page for that specific book opens (check URL); the title, author, publisher,
            a non-empty description, and ISBN are displayed.
            """)
    public void shouldVerifyBooksDetails() {
        Book book = Book.SPEAKING_JAVASCRIPT;

        new BookStorePage()
                .open()
                .shouldBeOpened()
                .clickOnBook(book.title())
                .shouldHaveUrlForBook(book.isbn())
                .shouldHaveTitle(book.title())
                .shouldHaveAuthor(book.author())
                .shouldHavePublisher(book.publisher())
                .shouldHaveDescription()
                .shouldHaveIsbn(book.isbn());
    }
}