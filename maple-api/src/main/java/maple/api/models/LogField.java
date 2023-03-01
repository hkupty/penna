package maple.api.models;

/**
 * The LogField enum is central to maple as it allows the customers to define which properties the log messages
 * will contain once printed to stdout.
 */
public enum LogField {

    /**
     * String representation of the level of the message that is being logged.
     */
    Level("level"),

    /**
     * Atomic long counter increasing at each message being logged.
     */
    Counter("counter"),

    /**
     * Name of the logger responsible for writing the message.
     */
    LoggerName("logger"),

    /**
     * The actual log message
     */
    Message("message"),

    /**
     * Markers are flexible pieces of data that somehow, depending on the usage and the interpretation
     * given to it, tag the message being logged.
     *
     * @see org.slf4j.Marker
     */
    Markers("tags"),

    /**
     * For newer SLF4J versions, messages can be logged using their fluent API. When done so, one has the
     * ability to add key-value data to the messages, which are logged separately from the main message.
     *
     * @see org.slf4j.event.KeyValuePair
     */
    KeyValuePairs("data"),

    /**
     * The name of the thread where the log originates
     */
    ThreadName("thread"),

    /**
     * Timestamp, in unix time, of the log message's creation.
     */
    Timestamp("timestamp"),

    /**
     * The log message can bear a throwable that will be logged as json data.
     */
    Throwable("throwable"),

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
    Extra("extra");

    /**
     * String property that will be used to render the field in the final json message.
     */
    public final String fieldName;
    LogField(String fieldName) {
        this.fieldName = fieldName;
    }
}
