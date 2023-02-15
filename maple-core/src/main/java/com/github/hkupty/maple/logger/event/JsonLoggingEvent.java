package com.github.hkupty.maple.logger.event;

import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonLoggingEvent implements LoggingEvent {
    private static Object[] emptyObjArray = new Object[]{};

    String logger;
    Level level;
    String message;
    List<Marker> markers;
    List<Object> arguments;
    List<KeyValuePair> keyValuePairs;
    Throwable throwable;
    String threadName;
    long timeStamp;

    String callerBoundary;

    public JsonLoggingEvent(Level level, String logger) {
        this.logger = logger;
        this.level = level;
    }

    public void addMarker(Marker marker) {
        if (markers == null) {
            markers = new ArrayList<>(2);
        }
        markers.add(marker);
    }

    @Override
    public List<Marker> getMarkers() {
        return markers;
    }

    public void addArgument(Object p) {
        getNonNullArguments().add(p);
    }

    public void addArguments(Object... args) {
        getNonNullArguments().addAll(Arrays.asList(args));
    }

    private List<Object> getNonNullArguments() {
        if (arguments == null) {
            arguments = new ArrayList<>(3);
        }
        return arguments;
    }

    @Override
    public List<Object> getArguments() {
        return arguments;
    }

    @Override
    public Object[] getArgumentArray() {
        if (arguments == null)
            return emptyObjArray;
        return arguments.toArray();
    }

    public void addKeyValue(String key, Object value) {
        getNonnullKeyValuePairs().add(new KeyValuePair(key, value));
    }

    private List<KeyValuePair> getNonnullKeyValuePairs() {
        if (keyValuePairs == null) {
            keyValuePairs = new ArrayList<>(4);
        }
        return keyValuePairs;
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    public void setThrowable(Throwable cause) {
        this.throwable = cause;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getLoggerName() {
        return logger;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    public String getThreadName() {
        return threadName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setCallerBoundary(String fqcn) {
        this.callerBoundary = fqcn;
    }

    public String getCallerBoundary() {
        return callerBoundary;
    }
}
