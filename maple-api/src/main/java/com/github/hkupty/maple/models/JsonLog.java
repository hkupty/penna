package com.github.hkupty.maple.models;

import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.Arrays;
import java.util.List;

/** Intentionally capped {@link LoggingEvent}.
 * This is because most of the "static" values can be reused by the providers;
 * This class will be given to providers, so, only the dynamic information from the logs will be used.
 *
 * @param arguments
 * @param level
 * @param markers
 * @param keyValuePairs
 * @param message
 * @param throwable
 */
public record JsonLog(
       Object[] arguments,
       Level level,
       Marker[] markers,
       KeyValuePair[] keyValuePairs,
       String message,
       Throwable throwable

) implements LoggingEvent {
    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getLoggerName() {
        return null;
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
        return 0;
    }

    @Override
    public String getThreadName() {
        return null;
    }
}
