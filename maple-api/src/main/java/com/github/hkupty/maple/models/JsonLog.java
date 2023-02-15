package com.github.hkupty.maple.models;

import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record JsonLog(
       Object[] arguments,
       String level,
       String logger,
       Marker[] markers,
       KeyValuePair[] keyValuePairs,
       String message,
       String threadName,
       Throwable throwable,
       long timestamp,

       Map<String, String> mdc
) implements LoggingEvent {
    @Override
    public Level getLevel() {
        return Level.valueOf(level);
    }

    @Override
    public String getLoggerName() {
        return logger;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<Object> getArguments() {
        return Arrays.asList(arguments);
    }

    @Override
    public Object[] getArgumentArray() {
        return arguments;
    }

    @Override
    public List<Marker> getMarkers() {
        return Arrays.asList(markers);
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return Arrays.asList(keyValuePairs);
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public long getTimeStamp() {
        return timestamp;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }
}
