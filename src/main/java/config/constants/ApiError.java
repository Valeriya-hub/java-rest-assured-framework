package config.constants;

/**
 * Expected API error: HTTP status together with the error code and message from the body.
 * The three values always belong together, so they are kept as one object
 * instead of three loose constants per error.
 */
public record ApiError(int status, String code, String message) {
}