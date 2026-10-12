package data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Reference data: books from the demoqa catalog that tests rely on.
 * The catalog is static, so these values are stable between runs.
 * If a value ever changes, it is fixed here, in one place.
 *
 * Scenario-specific inputs (for example the search text "Ruby") are NOT stored here:
 * they belong to the test that uses them.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Books {

    public static final Book GIT_POCKET_GUIDE = new Book(
            "Git Pocket Guide",
            "Richard E. Silverman",
            "O'Reilly Media",
            "9781449325862");

    public static final Book LEARNING_JAVASCRIPT_DESIGN_PATTERNS = new Book(
            "Learning JavaScript Design Patterns",
            "Addy Osmani",
            "O'Reilly Media",
            "9781449331818");

    public static final Book DESIGNING_EVOLVABLE_WEB_APIS = new Book(
            "Designing Evolvable Web APIs with ASP.NET",
            "Glenn Block et al.",
            "O'Reilly Media",
            "9781449337711");

    public static final Book SPEAKING_JAVASCRIPT = new Book(
            "Speaking JavaScript",
            "Axel Rauschmayer",
            "O'Reilly Media",
            "9781449365035");
}