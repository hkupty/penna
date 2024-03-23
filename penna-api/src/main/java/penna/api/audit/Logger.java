package penna.api.audit;

import penna.api.audit.internal.StdoutLogger;

/**
 * This class provides static methods for the logger to report back, specially during initialization.
 * It is supposed to be as simple as possible and as minimally used as possible.
 */
public class Logger {
    private Logger() {}
    private static final PseudoLogger impl = new StdoutLogger();

    /**
     * Reports through the underlying logger some important event. To be used sparingly.
     * @param level The target level.
     * @param event The message to be printed.
     */
    public static void report(String level, String event) {
        impl.report(level, event);
    }

    /**
     * Reports an error, usually in the form of an {@link Exception}. To be used sparingly, in situations
     * where the logger malfunctions and needs to explain upstream why it didn't work.
     * @param level The target level.
     * @param event The message to be printed.
     * @param throwable The exception or error that caused the report.
     */
    public static void reportError(String level, String event, Throwable throwable) {
        impl.reportError(level, event, throwable);
    }
}
