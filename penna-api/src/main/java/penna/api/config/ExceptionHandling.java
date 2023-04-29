package penna.api.config;

/**
 * This record holds configuration on how Penna should handle logging exception fields.
 * @param maxDepth How far the stacktrace can go before cutting short
 * @param deduplication When enabled, penna will not print repeated stack frames
 */
public record ExceptionHandling(
    int maxDepth,
    boolean deduplication
) {

    private static final ExceptionHandling singleton = new ExceptionHandling(64, false);

    /**
     * Gets the default instance.
     * @return The static default instance.
     */
    public static ExceptionHandling getDefault() {
        return singleton;
    }

    /**
     * Returns a copy of the original object swapping the value for {@link ExceptionHandling#deduplication}
     * @param deduplication Whether Penna should short-circuit out of writing stacktraces if identifies duplicates or not.
     * @return A copy of the original object with the values replaced
     */
    public ExceptionHandling replaceDeduplication(boolean deduplication) {
        return new ExceptionHandling(this.maxDepth, deduplication);
    }

    /**
     * Returns a copy of the original object swapping the value for {@link ExceptionHandling#maxDepth}
     * @param maxDepth The maximum number of stacktrace lines that should be shown before short-circuiting out
     * @return A copy of the original object with the values replaced
     */
    public ExceptionHandling replaceMaxDepth(int maxDepth) {
        return new ExceptionHandling(maxDepth, this.deduplication);
    }

    /**
     * This is a convenience method that returns a copy of the default object with the value for {@link ExceptionHandling#deduplication}
     * replaced by the parameter provided.
     * @param deduplication Whether Penna should short-circuit out of writing stacktraces if identifies duplicates or not.
     * @return An instance of {@link ExceptionHandling}
     */
    public static ExceptionHandling withDeduplication(boolean deduplication) {
        return singleton.replaceDeduplication(deduplication);
    }

    /**
     * This is a convenience method that returns a copy of the default object with the value for {@link ExceptionHandling#maxDepth}
     * replaced by the parameter provided.
     * @param maxDepth The maximum number of stacktrace lines that should be shown before short-circuiting out
     * @return An instance of {@link ExceptionHandling}
     */
    public static ExceptionHandling withMaxDepth(int maxDepth) {
        return singleton.replaceMaxDepth(maxDepth);
    }
}
