package tests.base;

import org.testng.SkipException;
/**
 * "Expected failure" marker for defects of the application under test that cannot be fixed here.
 *
 * The test asserts the DESIRED behaviour inside expectFailure():
 * - while the defect exists, the assertions fail and the test is reported as SKIPPED with the reason,
 *   so the suite stays green and the defect stays visible in the report;
 * - once the application is fixed, the assertions pass and the test FAILS with a reminder
 *   to remove the marker (and the README entry), so a fixed defect cannot stay hidden.
 */
public class KnownIssue {
    private static final int MAX_REASON_LENGTH = 300;

    private KnownIssue() {
    }

    public static void expectFailure(String reference, Runnable assertions) {
        try {
            assertions.run();
        } catch (AssertionError e) {
            throw new SkipException("Known issue (" + reference + "): " + shorten(e.getMessage()));
        }
        throw new AssertionError("Known issue (" + reference + ") appears to be fixed: "
                + "remove expectFailure() and the README entry");
    }

    // Assertion messages can contain a whole response body, which is too long for a skip reason.
    private static String shorten(String message) {
        if (message == null || message.length() <= MAX_REASON_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_REASON_LENGTH) + "...";
    }
}
