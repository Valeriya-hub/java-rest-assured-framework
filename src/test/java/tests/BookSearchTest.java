package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import ui.BookStorePage;

public class BookSearchTest extends BaseTest {

    @Feature("Book Store UI")
    @Story("Books Search")
    @Test(
            description = "Search for a book by its full title",
            groups = {"ui", "regression"}
    )
    @Description("""
            Preconditions: The user is not logged in, and the Book Store page is open.
            UI: The full book title 'Learning JavaScript Design Patterns' is entered into the search field.
            Verification: Only this book remains in the table; no other books are displayed.
            """)
    public void shouldVerifyBooksPageInitialState() {
        String bookTitle = "Learning JavaScript Design Patterns";
        String differentBookTitle = "Speaking JavaScript";

        new BookStorePage()
                .open()
                .shouldBeOpened()
                .searchBookByTitle(bookTitle)
                .shouldShowOnlyBook(bookTitle)
                .shouldNotShowBook(differentBookTitle);
    }

    @Feature("Book Store UI")
    @Story("Books Search")
    @Test(
            description = "Search by part of the title",
            groups = {"ui", "regression"}
    )
    @Description("""
            The user is not logged in; the Book Store page is open.
            UI: Part of the title, 'Web', is entered into the search field.
            Verification: The result is not empty; every displayed book contains the entered text;
            'Git Pocket Guide' is among the results.
            """)
    public void searchBookByPartialText() {
        String partialTitleForOneBook = "Web";
        String fullTitleForOneBook = "Designing Evolvable Web APIs with ASP.NET";
        String partialTitleForFewBook = "JavaScript";

        new BookStorePage()
                .open()
                .shouldBeOpened()
                .searchBookByTitle(partialTitleForOneBook)
                .shouldShowOnlyBooksContaining(partialTitleForOneBook)
                .shouldShowBook(fullTitleForOneBook)
                .searchBookByTitle(partialTitleForFewBook)
                .shouldShowOnlyBooksContaining(partialTitleForFewBook)
                .shouldShowSeveralBooksWithTitleContaining(partialTitleForFewBook);
    }

    @Feature("Book Store UI")
    @Story("Books Search")
    @Test(
            description = "Search with non-existing book",
            groups = {"ui", "regression"}
    )
    @Description("""
            The user is not logged in; the Book Store page is open.
            UI: Non-existing book, 'Ruby', is entered into the search field.
            Verification: The result is empty.
            """)
    public void searchNonExistingBook() {

        String nonExistingBookTitle = "Ruby";
        new BookStorePage()
                .open()
                .shouldBeOpened()
                .searchBookByTitle(nonExistingBookTitle)
                .shouldShowNoBooks();
    }
}