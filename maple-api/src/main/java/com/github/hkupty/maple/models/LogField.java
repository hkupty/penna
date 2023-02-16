package com.github.hkupty.maple.models;

public enum LogField {
    Level("level"),
    LoggerName("loggerName"),
    Message("message"),
    Markers("tags"),
    KeyValuePairs("data"),
    ThreadName("threadName"),
    Timestamp("timestamp"),
    Throwable("throwable"),
    MDC("mdc");

    private String fieldName;
    LogField(String fieldName) {
        this.fieldName = fieldName;
    }
}
