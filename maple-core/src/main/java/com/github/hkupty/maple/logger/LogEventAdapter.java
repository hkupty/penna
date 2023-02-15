package com.github.hkupty.maple.logger;

import com.github.hkupty.maple.models.JsonLog;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.LoggingEvent;

public class LogEventAdapter {
    private static final Object[] baseArguments = new Object[]{};
    private static final Marker[] baseMarkers = new Marker[]{};
    private static final KeyValuePair[] baseKeyValues = new KeyValuePair[]{};
    public static JsonLog transform(LoggingEvent loggingEvent) {

        Object[] arguments;
        Marker[] markers;
        KeyValuePair[] keyValuePairs;

        if (loggingEvent.getArgumentArray() != null){
            arguments = loggingEvent.getArgumentArray();
        } else {
            arguments = baseArguments;
        }

        if (loggingEvent.getMarkers() != null){
            markers = loggingEvent.getMarkers().toArray(baseMarkers);
        } else {
            markers = baseMarkers;
        }

        if (loggingEvent.getKeyValuePairs() != null){
            keyValuePairs = loggingEvent.getKeyValuePairs().toArray(baseKeyValues);
        } else {
            keyValuePairs = baseKeyValues;
        }

        return new JsonLog(
                arguments,
                loggingEvent.getLevel().toString(),
                loggingEvent.getLoggerName(),
                markers,
                keyValuePairs,
                loggingEvent.getMessage(),
                loggingEvent.getThreadName(),
                loggingEvent.getThrowable(),
                loggingEvent.getTimeStamp(),
                MDC.getCopyOfContextMap()
        );
    }
}
