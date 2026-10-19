package data;

/**
 * Immutable description of a book from the public demoqa BookStore catalog.
 * One object keeps title, author, publisher and ISBN together, so they can
 * never drift apart.
 *
 * Also contains static reference books from the demoqa catalog that tests rely on.
 */
public record Book(String title, String author, String publisher, String isbn) {

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