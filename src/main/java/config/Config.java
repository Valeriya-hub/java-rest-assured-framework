package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Config {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream is = Config.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new IllegalStateException(
                        "Файл config.properties не знайдено в classpath (src/test/resources)");
            }
            PROPERTIES.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося завантажити config.properties", e);
        }
    }

    private Config() {
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
                    "Властивість '" + key + "' не задана ні в config.properties, ні через -D" + key);
        }
        return value;
    }
}
