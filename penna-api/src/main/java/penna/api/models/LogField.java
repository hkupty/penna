package penna.api.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * The LogField enum is central to penna as it allows the customers to define which properties the log messages
 * will contain once printed to stdout.
 */
public enum LogField {

    /**
     * String representation of the level of the message that is being logged.
     */
    LEVEL("level"),

    /**
     * Atomic long counter increasing at each message being logged.
     */
    COUNTER("counter"),

    /**
     * Name of the logger responsible for writing the message.
     */
    LOGGER_NAME("logger"),

    /**
     * The actual log message
     */
    MESSAGE("message"),

    /**
     * Markers are flexible pieces of data that somehow, depending on the usage and the interpretation
     * given to it, tag the message being logged.
     *
     * @see org.slf4j.Marker
     */
    MARKERS("tags"),

    /**
     * For newer SLF4J versions, messages can be logged using their fluent API. When done so, one has the
     * ability to add key-value data to the messages, which are logged separately from the main message.
     *
     * @see org.slf4j.event.KeyValuePair
     */
    KEY_VALUE_PAIRS("data"),

    /**
     * The name of the thread where the log originates
     */
    THREAD_NAME("thread"),

    /**
     * Timestamp, in unix time, of the log message's creation.
     */
    TIMESTAMP("timestamp"),

    /**
     * The log message can bear a throwable that will be logged as json data.
     */
    THROWABLE("throwable"),

    /**
     * MDC is contextual information that is hierarchically bound to the thread and is added to the log message
     * when present.
     *
     * @see org.slf4j.MDC
     */
    MDC("mdc"),

    /**
     * Placeholder for user-added information.
     */
    EXTRA("extra");

    /**
     * String property that will be used to render the field in the final json message.
     */
    public final byte[] fieldName;

    LogField(@NotNull String fieldName) {
        this.fieldName = fieldName.getBytes();
    }

    /**
     * This is a utility static method to allow clients to retrieve LogFields based on their string representation.
     *
     * @param fieldName the string representation of the field to be logged
     * @return The respective enum value or null if none matched.
     */
    public static @Nullable LogField fromFieldName(@NotNull String fieldName) {
        @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
        var field = fieldName.getBytes();
        for (LogField value : values()) {
            if (Arrays.equals(value.fieldName, field)) {
                return value;
            }
        }
        return null;
    }
}
