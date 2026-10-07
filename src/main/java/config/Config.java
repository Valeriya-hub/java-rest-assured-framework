package config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public final class Config {
    private static final String ENV = System.getProperty("env", "qa");
    private static final Properties PROPERTIES = new Properties();

//    static {
//        try (InputStream is = Config.class.getClassLoader()
//                .getResourceAsStream("config.properties")) {
//            if (is == null) {
//                throw new IllegalStateException(
//                        "Файл config.properties не знайдено в classpath (src/test/resources)");
//            }
//            PROPERTIES.load(is);
//        } catch (IOException e) {
//            throw new IllegalStateException("Не вдалося завантажити config.properties", e);
//        }
//    }

    static {
        String resourceName = "config-" + ENV + ".properties";
        try (InputStream is = Config.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalStateException(
                        "Файл " + resourceName + " не знайдено в classpath (src/test/resources). "
                                + "Перевірте значення -Denv (поточне: '" + ENV + "').");
            }
            PROPERTIES.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося завантажити " + resourceName, e);
        }
    }

    private Config() {
    }

    /** Глобальний таймаут очікування елементів (мс). Можна перевизначити: -Dui.timeout.ms=10000 */
    public static long uiTimeoutMs() {
        return Long.getLong("ui.timeout.ms", 8_000L);
    }

    /** Інтервал polling-у Selenide (мс). */
    public static long uiPollingIntervalMs() {
        return Long.getLong("ui.polling.ms", 200L);
    }

    /** Таймаут завантаження сторінки (мс). */
    public static long pageLoadTimeoutMs() {
        return Long.getLong("ui.pageload.timeout.ms", 30_000L);
    }

    /** Лише для реально довгих операцій (генерація файлу, важкий async-процес). */
    public static Duration longOperationTimeout() {
        return Duration.ofSeconds(20);
    }

    public static String uiBaseUrl() {
        return get("ui.base.url");
    }

    public static String apiBaseUrl() {
        return get("api.base.url");
    }

    public static String bookStoreUrl() {
        return uiBaseUrl() + get("ui.path.books");
    }

    public static String loginUrl() {
        return uiBaseUrl() + get("ui.path.login");
    }

    public static String profileUrl() {
        return uiBaseUrl() + get("ui.path.profile");
    }

    public static String bookSearchUrl(String searchQuery) {
        return uiBaseUrl() + get("ui.path.books") + "?search=" + searchQuery;
    }

    private static String get(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Властивість '" + key + "' не задана ні в config-" + ENV + ".properties, ні через -D" + key);
        }
        return value;
    }
}
