package com.github.hkupty.maple.logger.event;

import com.github.hkupty.maple.logger.BaseLogger;
import com.github.hkupty.maple.models.JsonLog;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

public class JsonLogEventBuilder implements LoggingEventBuilder {

    private static final Object[] baseObjects = new Object[][]{};
    private static final Marker[] baseMarkers = new Marker[]{};
    private static final KeyValuePair[] baseKeyValues = new KeyValuePair[]{};

    private final Level level;
    private ArrayList<Marker> markers;
    private ArrayList<KeyValuePair> keyValuePairs;
    private ArrayList<Object> arguments;
    private String message;
    private final long timestamp;
    private Throwable throwable;
    private final BaseLogger logger;


    public JsonLogEventBuilder(BaseLogger logger, Level level) {
        this.level = level;
        this.logger = logger;

        // TODO revisit
        this.timestamp = System.currentTimeMillis();
    }


    @Override
    public LoggingEventBuilder setCause(Throwable cause) {
        this.throwable = cause;

        return this;
    }

    private ArrayList<Marker> getMarkers() {
        if (this.markers == null) {
            this.markers = new ArrayList<Marker>(2);
        }

        return markers;
    }
    @Override
    public LoggingEventBuilder addMarker(Marker marker) {
        getMarkers().add(marker);

        return this;
    }

    private ArrayList<Object> getArgumentsList() {
        if (this.arguments == null) {
            this.arguments = new ArrayList<Object>();
        }
        return this.arguments;
    }

    @Override
    public LoggingEventBuilder addArgument(Object p) {
        getArgumentsList().add(p);

        return this;
    }

    @Override
    public LoggingEventBuilder addArgument(Supplier<?> objectSupplier) {
        getArgumentsList().add(objectSupplier.get());

        return this;
    }

    private ArrayList<KeyValuePair> getKeyValueList() {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = new ArrayList<KeyValuePair>();
        }
        return this.keyValuePairs;
    }

    @Override
    public LoggingEventBuilder addKeyValue(String key, Object value) {
        getKeyValueList().add(new KeyValuePair(key, value));
        return this;
    }

    @Override
    public LoggingEventBuilder addKeyValue(String key, Supplier<Object> valueSupplier) {
        getKeyValueList().add(new KeyValuePair(key, valueSupplier.get()));
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(Supplier<String> messageSupplier) {
        this.message = messageSupplier.get();

        return this;
    }

    private JsonLog build() {
        return new JsonLog(
                arguments != null ? arguments.toArray(baseObjects) : baseObjects,
                level.name(),
                logger.getName(),
                markers != null ? markers.toArray(baseMarkers) : baseMarkers,
                keyValuePairs != null ? keyValuePairs.toArray(baseKeyValues) : baseKeyValues,
                message,
                "pedro",
                throwable,
                timestamp,
                MDC.getCopyOfContextMap()
        );
    }

    @Override
    public void log() {
        log(build());
    }

    private void log(LoggingEvent loggingEvent) {
        logger.log(loggingEvent);
    }

    @Override
    public void log(String message) {
        setMessage(message);
        log();
    }

    @Override
    public void log(String message, Object arg) {
        addArgument(arg);
        setMessage(message);
        log();
    }

    @Override
    public void log(String message, Object arg0, Object arg1) {
        addArgument(arg0);
        addArgument(arg1);
        setMessage(message);
        log();
    }

    @Override
    public void log(String message, Object... args) {
        addArguments(args);
        setMessage(message);
        log();
    }

    public void addArguments(Object... args) {
        getArgumentsList().addAll(Arrays.asList(args));
    }

    @Override
    public void log(Supplier<String> messageSupplier) {
        setMessage(messageSupplier);
        log();
    }
}