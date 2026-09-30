package auth;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;
import org.openqa.selenium.Cookie;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.open;

/**
 * Дозволяє "залогінити" браузер без проходження UI-форми логіну:
 * підставляємо cookie 'userID', 'token', 'userName' та 'expires',
 * отримані через API, і одразу відкриваємо приватну сторінку профілю.
 *
 * NB: сайт виставляє ці cookie не через HTTP-заголовок Set-Cookie,
 * а клієнтським JS (document.cookie) після логіну — на роботу
 * WebDriver.manage().addCookie(...) це не впливає, оскільки для
 * React-застосунку важливий лише кінцевий стан cookie-стора браузера.
 */
public class AuthSession {
    @Step("UI: підставити auth cookie (userID, token, userName) для юзера {userId}")
    public static void injectAuthCookies(String userId, String userName, String token, String expiresIso) {
        // Cookie можна додати лише для домену, на якому вже "стоїть" браузер,
        // тому спочатку відкриваємо будь-яку сторінку цього ж домену.
        open("/");

        addCookieIfPresent("userID", userId);
        addCookieIfPresent("userName", userName);
        addCookieIfPresent("token", token);
        // 'expires' у застосунку — це саме значення cookie (ISO-рядок), а не TTL самої cookie:
        // на реальній сесії Selenide/Selenium-кука теж лишається "Session" (без setExpiry).
        addCookieIfPresent("expires", expiresIso);
    }

    private static void addCookieIfPresent(String name, String value) {
        if (value == null) {
            return;
        }
        WebDriverRunner.getWebDriver().manage().addCookie(new Cookie(name, value));
    }

    @Step("UI: отримати токен авторизації з cookie браузера")
    public static String getAuthTokenFromCookie() {
        Cookie tokenCookie = Selenide.Wait()
                .withTimeout(Duration.ofSeconds(10))
                .until(driver -> WebDriverRunner.getWebDriver().manage().getCookieNamed("token"));
        return tokenCookie.getValue();
    }
}