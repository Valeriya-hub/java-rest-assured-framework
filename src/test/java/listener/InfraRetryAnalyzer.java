package listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.List;

/**
 * Retries a test ONCE, and only when it failed because of the browser infrastructure
 * (the browser did not start or the renderer did not answer).
 * <p>
 * Assertion failures and any other errors are never retried, so a real product defect
 * is not hidden. Every retried attempt stays visible in the Allure report (Retries tab).
 */
public class InfraRetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(InfraRetryAnalyzer.class);

    private static final int MAX_RETRIES = 1;

    // Fragments of error messages that identify a browser start-up / renderer problem
    private static final List<String> INFRA_ERROR_MARKERS = List.of(
            "Timed out receiving message from renderer",
            "Chrome instance exited",
            "session not created"
    );

    private int retries = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retries >= MAX_RETRIES || !isInfrastructureFailure(result.getThrowable())) {
            return false;
        }
        retries++;
        log.warn("Retrying '{}' after an infrastructure failure (retry {} of {})",
                result.getName(), retries, MAX_RETRIES);
        return true;
    }

    private static boolean isInfrastructureFailure(Throwable error) {
        // The marker may be in the exception itself or in any of its causes
        for (Throwable current = error; current != null; current = current.getCause()) {
            String message = current.getMessage();
            if (message != null && INFRA_ERROR_MARKERS.stream().anyMatch(message::contains)) {
                return true;
            }
            if (current.getCause() == current) {
                break;
            }
        }
        return false;
    }
}