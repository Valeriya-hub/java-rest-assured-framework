package tests;

import data.Book;
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
            UI: The full book title is entered into the search field.
            Verification: Only this book remains in the table; no other books are displayed.
            """)
    public void searchBookByFullTitle() {
        Book book = Book.LEARNING_JAVASCRIPT_DESIGN_PATTERNS;

        new BookStorePage()
                .open()
                .shouldBeOpened()
                .searchBookByTitle(book.title())
                .shouldShowOnlyBook(book.title())
                .shouldNotShowBook(Book.SPEAKING_JAVASCRIPT.title());
    }

    @Feature("Book Store UI")
    @Story("Books Search")
    @Test(
            description = "Search by part of the title",
            groups = {"ui", "regression"}
    )
    @Description("""
            The user is not logged in; the Book Store page is open.
            UI: Part of the title is entered into the search field.
            Verification: The result is not empty; every displayed book contains the entered text.
            """)
    public void searchBookByPartialText() {
        String partialTitleForOneBook = "Web";
        Book bookWithWebInTitle = Book.DESIGNING_EVOLVABLE_WEB_APIS;
        String partialTitleForFewBook = "JavaScript";

        new BookStorePage()
                .open()
                .shouldBeOpened()
                .searchBookByTitle(partialTitleForOneBook)
                .shouldShowOnlyBooksContaining(partialTitleForOneBook)
                .shouldShowBook(bookWithWebInTitle.title())
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
            UI: Non-existing book is entered into the search field.
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