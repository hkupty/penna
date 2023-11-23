package penna.core.logger;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventBuilder;
import penna.core.internals.LogUnitContextPool;
import penna.core.minilog.MiniLogger;
import penna.core.models.KeyValuePair;
import penna.core.models.PennaLogEvent;
import penna.core.sink.Sink;

import java.io.IOException;
import java.util.function.Supplier;

public record LogUnitContext(
        LogUnitContextPool pool,
        int selfReference,
        Sink sink,
        PennaLogEvent logEvent
) implements LoggingEventBuilder {

    public void fromLoggingEvent(LoggingEvent event) {
        if (event instanceof PennaLogEvent pennaLogEvent) {
            fromPennaEvent(pennaLogEvent);
            return;
        }

        setCause(event.getThrowable());
        setMessage(event.getMessage());
        addArguments(event.getArgumentArray());
        for (var kvp : event.getKeyValuePairs()) {
            addKeyValue(kvp.key, kvp.value);
        }
        for (var marker : event.getMarkers()) {
            addMarker(marker);
        }

        log();
    }

    private void fromPennaEvent(PennaLogEvent event) {
        setCause(event.throwable);
        setMessage(event.message);
        addArguments(event.arguments);
        for (var kvp : event.keyValuePairs) {
            addKeyValue(kvp.key(), kvp.value());
        }
        for (var marker : event.markers) {
            addMarker(marker);
        }

        log();
    }

    private void release() {
        pool.release(selfReference);
    }

    public void reset(PennaLogger logger, Level level) {
        logEvent.reset(logger.nameAsChars, logger.config, level, Thread.currentThread());
    }

    @Override
    public LoggingEventBuilder setCause(Throwable cause) {
        logEvent.throwable = cause;
        return this;
    }

    @Override
    public LoggingEventBuilder addMarker(Marker marker) {
        logEvent.markers.add(marker);
        return this;
    }

    @Override
    public LoggingEventBuilder addArgument(Object p) {
        if (p instanceof Throwable throwable) {
            setCause(throwable);
        } else {
            logEvent.addArgument(p);
        }
        return this;
    }

    @Override
    public LoggingEventBuilder addArgument(Supplier<?> objectSupplier) {
        return addArgument(objectSupplier.get());
    }

    @Override
    public LoggingEventBuilder addKeyValue(String key, Object value) {
        logEvent.keyValuePairs.add(new KeyValuePair(key, value));
        return this;
    }

    @Override
    public LoggingEventBuilder addKeyValue(String key, Supplier<Object> valueSupplier) {
        return addKeyValue(key, valueSupplier.get());
    }

    @Override
    public LoggingEventBuilder setMessage(String message) {
        logEvent.message = message;
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(Supplier<String> messageSupplier) {
        logEvent.message = messageSupplier.get();
        return this;
    }

    @Override
    public void log() {
        try {
            sink.write(logEvent);
        } catch (IOException e) {
            MiniLogger.error("Unable to write log.", e);
        } finally {
            release();
        }
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
        logEvent.addAllArguments(args);
    }

    @Override
    public void log(Supplier<String> messageSupplier) {
        setMessage(messageSupplier);
        log();
    }
}
