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
}
