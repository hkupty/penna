package maple.api.models;

/**
 * The LogField enum is central to maple as it allows the customers to define which properties the log messages
 * will contain once printed to stdout.
 */
public enum LogField {
    Level("level"),
    Counter("counter"),
    LoggerName("logger"),
    Message("message"),
    Markers("tags"),
    KeyValuePairs("data"),
    ThreadName("thread"),
    Timestamp("timestamp"),
    Throwable("throwable"),
    MDC("mdc"),
    Extra("extra");

    public final String fieldName;
    LogField(String fieldName) {
        this.fieldName = fieldName;
    }
}
