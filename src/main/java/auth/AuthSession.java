package auth;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;
import org.openqa.selenium.Cookie;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.open;

/**
 * Allows the browser to be authenticated without going through the UI login form:
 * we set the 'userID', 'token', 'userName', and 'expires' cookies
 * obtained via the API, and immediately open the private profile page.
 *
 * NB: The website does not set these cookies via the HTTP Set-Cookie header.
 * Instead, client-side JavaScript (document.cookie) sets them after login.
 * This does not affect WebDriver.manage().addCookie(...), because
 * the React application only cares about the final state of the browser's
 * cookie store.
 */
public class AuthSession {
    @Step("UI: Set authentication cookies (userID, token, userName) for user {userId}")
    public static void injectAuthCookies(String userId, String userName, String token, String expiresIso) {
        // Cookies can only be added for a domain the browser is already on,
        // so we first open any page on the same domain.
        open("/");

        addCookieIfPresent("userID", userId);
        addCookieIfPresent("userName", userName);
        addCookieIfPresent("token", token);
        // In the application, 'expires' is the cookie value itself (an ISO string),
        // not the cookie's TTL. In a real Selenide/Selenium session, the cookie
        // also remains a session cookie (without setExpiry).
        addCookieIfPresent("expires", expiresIso);
    }

    private static void addCookieIfPresent(String name, String value) {
        if (value == null) {
            return;
        }
        WebDriverRunner.getWebDriver().manage().addCookie(new Cookie(name, value));
    }

    @Step("UI: get the authorization token from the browser cookie")
    public static String getAuthTokenFromCookie() {
        Cookie tokenCookie = Selenide.Wait()
                .withTimeout(Duration.ofSeconds(10))
                .until(driver -> WebDriverRunner.getWebDriver().manage().getCookieNamed("token"));
        return tokenCookie.getValue();
    }
}