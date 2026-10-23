package util;

public class WikiRequestException extends RuntimeException {

    public enum Kind {
        NO_INTERNET,
        SERVER_UNAVAILABLE,
        TIMEOUT,
        BAD_RESPONSE,
        INTERRUPTED
    }

    private final Kind kind;

    public WikiRequestException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind getKind() { return kind; }
}