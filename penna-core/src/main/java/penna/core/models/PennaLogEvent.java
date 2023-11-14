package penna.core.models;

import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PennaLogEvent implements LoggingEvent {
    private int cursor;
    public Object[] arguments = new Object[8];
    public List<Marker> markers = new ArrayList<>();
    public List<KeyValuePair> keyValuePairs = new ArrayList<>();
    public Object extra;
    public Level level;
    public String message;
    public byte[] threadName;
    public Throwable throwable;
    public byte[] logger;
    public LogConfig config;
    public long timestamp;

    /**
     * Resets all the fields that will change during log creation.
     * <br />
     * Note! ThreadName is never changing for the lifetime of the log object.
     * This is because it is bound to the object pool in the LoggingEventBuilder and,
     * therefore, will never change.
     */
    public void reset() {
        markers.clear();
        cursor = 0;
        Arrays.fill(arguments, null);
        keyValuePairs.clear();

        extra = null;
        message = null;
        throwable = null;
        logger = null;
        timestamp = 0;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getLoggerName() {
        return new String(logger);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<Object> getArguments() {
        return Arrays.asList(arguments);
    }

    @SuppressWarnings("PMD.MethodReturnsInternalArray")
    @Override
    public Object[] getArgumentArray() {
        return arguments;
    }

    public void addArgument(Object argument) {
        if (cursor + 1 >= arguments.length) {
            arguments = Arrays.copyOf(arguments, arguments.length * 2);
        }
        arguments[cursor++] = argument;
    }

    public void addAllArguments(Object... newArguments) {
        if (cursor + newArguments.length > arguments.length) {
            arguments = Arrays.copyOf(arguments, arguments.length + newArguments.length * 2);
        }
        System.arraycopy(newArguments, 0, arguments, cursor, newArguments.length);
    }

    @Override
    public List<Marker> getMarkers() {
        return markers;
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
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
        return new String(threadName);
    }

    @Override
    public String toString() {
        return "PennaLogEvent{" +
                "cursor=" + cursor +
                ", arguments=" + Arrays.toString(arguments) +
                ", markers=" + markers +
                ", keyValuePairs=" + keyValuePairs +
                ", extra=" + extra +
                ", level=" + level +
                ", message='" + message + '\'' +
                ", threadName=" + Arrays.toString(threadName) +
                ", throwable=" + throwable +
                ", logger=" + Arrays.toString(logger) +
                ", config=" + config +
                ", timestamp=" + timestamp +
                '}';
    }
}