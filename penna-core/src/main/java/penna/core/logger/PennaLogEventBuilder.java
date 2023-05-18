package penna.core.logger;

import penna.core.minilog.MiniLogger;
import penna.core.models.PennaLogEvent;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LoggingEventBuilder;
import penna.core.sink.PennaSink;
import penna.core.sink.SinkImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Penna's {@link LoggingEventBuilder} implementation.
 * It relies on two major concepts:
 * a) object pooling; and
 * b) thread locals.
 * <br />
 * <strong>Object Pooling</strong>
 * Each instance of PennaLogEventBuilder will have a small pool of objects.
 * This isn't strictly necessary - we could reuse a single instance - but allows for some flexibility.
 * The main idea here is that the {@link PennaLogEventBuilder} will never create objects in runtime.
 * Instead, it will reuse existing objects in a pool. This has two main reasons:
 *  - as a logger, penna doesn't cause pressure in the GC;
 *  - given {@link PennaLogEvent}s are simple vessels for data, we can reuse them at will;
 * <br />
 * <strong>Thread Local</strong>
 * {@link PennaLogEventBuilder} has to be unique for every thread. This is because of the object pool:
 * we don't want the log object to be overwritten by another thread. Instead, what we do is we ensure
 * every thread gets one set of objects + pointers to work with, which allows us to safely control
 * the pool and ensure no two different threads are operating on the same log object.
 * That is why we can only create the builders through the static {@link Factory}.
 */
public final class PennaLogEventBuilder implements LoggingEventBuilder {
    public static final int POOL_SIZE = 16;
    private final PennaLogEvent[] pool;
    private int currentIndex;
    SinkImpl sink;
    private PennaLogEvent current;

    public final static class Factory {

        private Factory() {}
        private final static ThreadLocal<PennaLogEventBuilder> pool = ThreadLocal.withInitial(PennaLogEventBuilder::new);

        public static void fromLoggingEvent(PennaLogger logger, LoggingEvent event) {
            var builder = pool.get();
            builder.next();
            builder.setCause(event.getThrowable());
            builder.setMessage(event.getMessage());
            builder.addArguments(event.getArgumentArray());
            for (var kvp : event.getKeyValuePairs()) {
                builder.addKeyValue(kvp.key, kvp.value);
            }
            for (var marker : event.getMarkers()) {
                builder.addMarker(marker);
            }

            builder.current.level = event.getLevel();
            builder.current.logger = logger;
            builder.current.config = logger.config;

            builder.log();
        }

        public static PennaLogEventBuilder get(PennaLogger logger, Level level) {
            var builder = pool.get();
            builder.next();
            builder.current.logger = logger;
            builder.current.level = level;
            builder.current.config = logger.config;

            return builder;
        }

        // DO NOT USE THIS METHOD.
        static void replaceSinkLocally(SinkImpl sink) {
            pool.get().sink = sink;
        }
    }

    /**
     * Resets the builder so the next LogEvent is ready to be used.
     */
    private void next() {
        // This is only possible because POOL_SIZE is a power of 2
        currentIndex = (currentIndex + 1) & (POOL_SIZE - 1);
        current = pool[currentIndex];
        current.reset();
    }

    private PennaLogEventBuilder() {
        String threadName = Thread.currentThread().getName();
        // TODO Maybe we need multiple sinks again based on layout.
        sink = PennaSink.getSink();
        pool = new PennaLogEvent[POOL_SIZE];
        for (int i = 0; i < POOL_SIZE; i++){
            pool[i] = new PennaLogEvent();
            pool[i].threadName = threadName;
        }

        current = pool[currentIndex];
    }

    @Override
    public LoggingEventBuilder setCause(Throwable cause) {
        this.current.throwable = cause;
        return this;
    }

    private List<Marker> getMarkers() {
        return this.current.markers;
    }
    @Override
    public LoggingEventBuilder addMarker(Marker marker) {
        getMarkers().add(marker);
        return this;
    }

    private List<Object> getArgumentsList() {
        return this.current.arguments;
    }

    @Override
    public LoggingEventBuilder addArgument(Object p) {
        if (p instanceof Throwable throwable) {
            setCause(throwable);
        } else {
            getArgumentsList().add(p);
        }
        return this;
    }

    @Override
    public LoggingEventBuilder addArgument(Supplier<?> objectSupplier) {
        getArgumentsList().add(objectSupplier.get());
        return this;
    }

    private List<KeyValuePair> getKeyValueList() {
        return this.current.keyValuePairs;
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
        this.current.message = message;
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(Supplier<String> messageSupplier) {
        this.current.message = messageSupplier.get();
        return this;
    }

    @Override
    public void log() {
        if (!this.current.arguments.isEmpty()) {
            this.current.message = MessageFormatter.basicArrayFormat(this.current.message, this.current.getArgumentArray());
        }

        try {
            sink.write(this.current);
        } catch (IOException e) {
            MiniLogger.error("Unable to write log.", e);
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
        getArgumentsList().addAll(Arrays.asList(args));
    }

    @Override
    public void log(Supplier<String> messageSupplier) {
        setMessage(messageSupplier);
        log();
    }
}
