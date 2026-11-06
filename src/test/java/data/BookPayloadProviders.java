package data;

import org.testng.annotations.DataProvider;

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
}
