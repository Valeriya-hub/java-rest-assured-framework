package ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import config.Config;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static config.constants.UIConstants.BOOK_DELETED_ALERT;

public class ProfilePage extends BasePage<ProfilePage> {
    private static final String BOOK_ROW_XPATH_TEMPLATE =
            "//div[@class='action-buttons']//a[contains(@href, '%s')]/ancestor::tr[1]";
    private final SelenideElement userNameLabel = $("#userName-value");
    private final SelenideElement goToBookStoreButton = $("#gotoStore");
    private final SelenideElement deleteBookModal = $(".modal-content");
    private final SelenideElement okButtonOnModal = $("#closeSmallModal-ok");
    private final SelenideElement logoutButton = $x("//button[text()='Logout']");
    private final SelenideElement notLoggedInWrapper = $("#notLoggin-wrapper");
    private final SelenideElement notLoggedInLabel = $("#notLoggin-label");
    private final SelenideElement loginLink = $("#notLoggin-label a[href='/login']");
    private final SelenideElement registerLink = $("#notLoggin-label a[href='/register']");

    @Step("Open profile page")
    public ProfilePage open() {
        return openPage(Config.profileUrl());
    }

    @Step("Verify that profile page is opened")
    public ProfilePage shouldBeOpened() {
        webdriver().shouldHave(urlContaining(Config.profileUrl()));
        return this;
    }

    @Step("Verify that user name {expectedUserName} is displayed on profile page")
    public ProfilePage shouldSeeUserName(String expectedUserName) {
        userNameLabel.shouldHave(exactText(expectedUserName).because("User name should match the one used for login"));
        return this;
    }

    @Step("Navigate to Book Store from profile page")
    public BookStorePage clickGoToBookStoreButton() {
        goToBookStoreButton.click();
        return new BookStorePage();
    }

    private SelenideElement bookRow(String isbn) {
        String xpath = String.format(BOOK_ROW_XPATH_TEMPLATE, isbn);
        return $x(xpath);
    }

    @Step("Verify that book with ISBN {isbn} is displayed in the table")
    public ProfilePage shouldBookVisible(String isbn) {
        bookRow(isbn).shouldBe(visible);
        return this;
    }

    @Step("Verify that book with ISBN {isbn} is not in the table")
    public void shouldBookNotBeVisible(String isbn) {
        bookRow(isbn).shouldNot(Condition.exist);
    }

    private SelenideElement deleteIconInRow(String isbn) {
        return $("#delete-record-" + isbn);
    }

    @Step("Delete book with ISBN {isbn} via Trash icon with confirmation")
    public ProfilePage deleteBook(String isbn) {
        deleteIconInRow(isbn).click();
        confirmDeletionInModal();
        acceptBookDeletedAlert();

        // The negative check is only meaningful because the modal was seen open above:
        // it proves the locator matches the real modal, so this cannot pass by accident.
        deleteBookModal.shouldNotBe(visible.because("The delete confirmation modal should be closed"));
        return this;
    }

    @Step("Confirm deletion in the modal window")
    private void confirmDeletionInModal() {
        deleteBookModal.shouldBe(visible.because("The delete confirmation modal should be opened"));
        okButtonOnModal.shouldBe(clickable).scrollTo().click();
    }

    /**
     * The app reports the result with a native JS alert. It must be accepted explicitly:
     * confirm() waits for the alert, checks its text and clicks OK. Otherwise, the alert
     * can pop up in the middle of the next command and break it.
     */
    @Step("Accept the browser alert about the deleted book")
    private void acceptBookDeletedAlert() {
        try {
            confirm(BOOK_DELETED_ALERT);
        } catch (Throwable e) {
            // If the alert did not appear due to a server/network delay during a parallel run,
            // try clicking the confirmation button in the modal again, if it is still open
            if (deleteBookModal.is(visible)) {
                okButtonOnModal.click();
                confirm(BOOK_DELETED_ALERT);
            } else {
                throw e; // If the modal closed but the alert didn't actually appear, throw an error.
            }
        }
    }

    @Step("Verify that Logout button is visible")
    public ProfilePage shouldSeeLogoutButton() {
        logoutButton.shouldBe(visible.because("Logout button should be visible after successful login"));
        return this;
    }

    @Step("Click on Logout button and redirect to Login page")
    public LoginPage clickLogoutButton() {
        logoutButton.click();
        return new LoginPage();
    }

    @Step("Verify unauthorized user message and links on Profile page")
    public void shouldSeeUnauthenticatedState() {
        notLoggedInWrapper.shouldBe(visible
                .because("Unauthorized warning wrapper should be displayed on /profile"));

        notLoggedInLabel.shouldHave(text("Currently you are not logged into the Book Store application")
                .because("Text informing that user is not logged in should be visible"));

        loginLink.shouldBe(visible
                .because("Link to Login page should be displayed inside warning text"));

        registerLink.shouldBe(visible
                .because("Link to Register page should be displayed inside warning text"));

    }
}