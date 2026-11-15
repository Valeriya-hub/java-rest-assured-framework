package tests.ui;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;
import tests.base.BaseTest;
import tests.base.KnownIssue;
import ui.BookStorePage;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BookCoverIntegrityTest extends BaseTest {
    private static final String KNOWN_ISSUE =
            "demoqa: book covers are bound to the grid row, not to the book";
    private static final int BOOKS_TO_SEARCH = 3;

    @Feature("Book Store")
    @Story("Search keeps book data consistent")
    @Test(
            description = "Cover image stays with its book after searching by full title",
            groups = {"regression", "ui"}
    )
    @Description("""
            Known defect: covers are bound to grid rows (the site cycles through 4 images by row index).
            Steps: remember the cover of every book in the full list -> search several books by full title.
            Only books whose cover differs from the first row's cover are searched: a found book
            lands in row 1, so a book with the same cover as row 1 would pass even with the defect.
            Expected: the found book shows ITS OWN cover.
            Actual (defect): it shows the cover of row 1 (README, Findings #13).
            Until it is fixed the test is reported as skipped;
            when the behaviour is fixed, the test fails to remind that the marker must be removed.
            """)
    public void coverIsKeptAfterSearchByFullTitle() {
        BookStorePage page = new BookStorePage().open();
        page.shouldBooksTableVisible();

        Map<String, String> baseline = page.getCoverSrcByTitle();
        assertBaselineIsUsable(baseline);

        String firstRowCover = baseline.values().iterator().next();
        List<String> titlesToSearch = baseline.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(firstRowCover))
                .map(Map.Entry::getKey)
                .limit(BOOKS_TO_SEARCH)
                .toList();
        assertThat(titlesToSearch)
                .as("Books whose cover differs from the first row's cover")
                .isNotEmpty();

        // All UI interaction (and its Selenide waits) happens BEFORE expectFailure.
        Map<String, Map<String, String>> coversAfterSearch = new LinkedHashMap<>();
        for (String title : titlesToSearch) {
            page.searchAndWaitForFilter(title);
            coversAfterSearch.put(title, page.getCoverSrcByTitle());
        }

        // Only the data comparison is inside, so a UI timeout is never reported as a known issue.
        KnownIssue.expectFailure(KNOWN_ISSUE, () -> {
            SoftAssertions softly = new SoftAssertions();
            coversAfterSearch.forEach((title, covers) ->
                    softly.assertThat(covers.get(title))
                            .as("Cover of '%s' after searching by its full title", title)
                            .isEqualTo(baseline.get(title)));
            softly.assertAll();
        });
    }

    @Feature("Book Store")
    @Story("Search keeps book data consistent")
    @Test(
            description = "Covers stay with their books after searching by part of the title",
            groups = {"regression", "ui"}
    )
    @Description("""
            Steps: remember the cover of every book in the full list -> search 'JavaScript'.
            Expected: every found book keeps the cover it had in the full list.
            
            Known issue: covers are re-assigned by row, e.g. 'Learning JavaScript Design Patterns'
            had the 'Silence' cover in the full list but shows 'Business Design' after the search
            (README, Findings #13). Until it is fixed the test is reported as skipped;
            when the behaviour is fixed, the test fails to remind that the marker must be removed.
            """)
    public void coversAreKeptAfterSearchByPartialTitle() {
        String query = "JavaScript";

        BookStorePage page = new BookStorePage().open();
        page.shouldBooksTableVisible();

        Map<String, String> baseline = page.getCoverSrcByTitle();
        assertBaselineIsUsable(baseline);

        page.searchAndWaitForFilter(query);
        Map<String, String> afterSearch = page.getCoverSrcByTitle();
        assertThat(afterSearch)
                .as("Search by '%s' must return several books", query)
                .hasSizeGreaterThan(1);

        KnownIssue.expectFailure(KNOWN_ISSUE, () -> {
            SoftAssertions softly = new SoftAssertions();
            afterSearch.forEach((title, src) ->
                    softly.assertThat(src)
                            .as("Cover of '%s' after searching by '%s'", title, query)
                            .isEqualTo(baseline.get(title)));
            softly.assertAll();
        });
    }

    /**
     * Covers are NOT unique per book on demoqa (the same few images repeat), so we only require
     * that there is more than one distinct cover. With a single cover a mix-up is undetectable.
     */
    private void assertBaselineIsUsable(Map<String, String> baseline) {
        assertThat(baseline).as("Full book list").hasSizeGreaterThan(2);
        assertThat(new HashSet<>(baseline.values()))
                .as("The full list must contain at least two different covers")
                .hasSizeGreaterThan(1);
    }
}