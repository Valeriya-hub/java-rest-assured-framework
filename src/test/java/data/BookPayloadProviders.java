package data;

import org.testng.annotations.DataProvider;

import java.util.Map;
import java.util.UUID;

/**
 * Data providers for the BookStore API tests.
 */
public class BookPayloadProviders {
    /**
     * ISBN values that the API rejects with 400 and the 'ISBN not available' error.
     * The first column only names the case in the test report.
     */
    @DataProvider(name = "unavailableIsbns")
    public static Object[][] unavailableIsbns() {
        return new Object[][]{
                {"13 digits, but not in the catalog", "9780000000000"},
                {"not an ISBN at all", "invalid-isbn"},
                {"empty value", ""}
        };
    }

    /**
     * Entries of collectionOfIsbns that the API rejects with 400 and the 'ISBN not available' error.
     * The first column only names the case in the test report; an empty map is an entry without 'isbn'.
     */
    @DataProvider(name = "unavailableIsbnEntries")
    public static Object[][] unavailableIsbnEntries() {
        return new Object[][]{
                {"13 digits, but not in the catalog", Map.of("isbn", "9780000000000")},
                {"not an ISBN at all", Map.of("isbn", "invalid-isbn")},
                {"empty isbn value", Map.of("isbn", "")},
                {"isbn field is absent", Map.of()}
        };
    }

    /**
     * userId values that the API rejects with 401 and the 'User Id not correct' error.
     */
    @DataProvider(name = "incorrectUserIds")
    public static Object[][] incorrectUserIds() {
        return new Object[][]{
                {"empty userId", ""},
                {"non-existent userId", UUID.randomUUID().toString()}
        };
    }

    /**
     * Tokens that the API rejects with 401 and the 'User not authorized!' error.
     * The first column only names the case in the test report; null means "no Authorization header".
     */
    @DataProvider(name = "unauthorizedTokens")
    public static Object[][] unauthorizedTokens() {
        return new Object[][]{
                {"no Authorization header", null},
                {"malformed token", "invalid.token.value"}
        };
    }

    /**
     * ISBNs that are not in the user's collection (the user owns only GIT_POCKET_GUIDE).
     * The API rejects them with 400 and the 'ISBN not available in User's Collection' error.
     */
    @DataProvider(name = "isbnsNotInCollection")
    public static Object[][] isbnsNotInCollection() {
        return new Object[][]{
                {"book exists in the catalog, but not in this collection", Book.SPEAKING_JAVASCRIPT.isbn()},
                {"13 digits, but not in the catalog", "9780000000000"},
                {"empty isbn value", ""}
        };
    }
}
