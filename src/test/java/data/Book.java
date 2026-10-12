package data;

/**
 * Immutable description of a book from the public demoqa BookStore catalog.
 * One object keeps title, author, publisher and ISBN together, so they can
 * never drift apart (e.g. the title of one book combined with the ISBN of another).
 */
public record Book(String title, String author, String publisher, String isbn) {
}