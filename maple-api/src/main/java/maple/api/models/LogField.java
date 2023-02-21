package maple.api.models;

public enum LogField {
    Level("level"),
    Counter("counter"),
    LoggerName("loggerName"),
    Message("message"),
    Markers("tags"),
    KeyValuePairs("data"),
    ThreadName("threadName"),
    Timestamp("timestamp"),
    Throwable("throwable"),
    MDC("mdc"),
    Extra("extra");

    public final String fieldName;
    LogField(String fieldName) {
        this.fieldName = fieldName;
    }
}
