package maple.core.logger.event;

import maple.api.models.MapleLogEvent;
import maple.core.logger.MapleLogger;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class JsonLogEventBuilder implements LoggingEventBuilder {

    private MapleLogEvent current;
    private String threadName;

    public static class Factory {
        private static final MapleLogEvent[] pool = new MapleLogEvent[2048];
        private static int counter;
        private static final ThreadLocal<JsonLogEventBuilder> local = ThreadLocal.withInitial(JsonLogEventBuilder::new);

        private static final ReentrantLock lock;

        static {
            lock = new ReentrantLock();
            for(int i = 0; i < pool.length; i++) {
                pool[i] = new MapleLogEvent();
            }
            counter = 0;
        }

        public static JsonLogEventBuilder get(MapleLogger logger, Level level) {
            try {
                lock.lock();
                var builder = local.get();
                builder.current = pool[counter];
                builder.current.reset();
                builder.current.fieldsToLog = logger.getFieldsToLog();
                builder.current.threadName = builder.threadName;
                builder.current.logger = logger;
                builder.current.level = level;

                counter = (counter + 1) & (pool.length - 1);

                return builder;
            } finally {
                lock.unlock();
            }
        }

        public static MapleLogEvent fromLoggingEvent(LoggingEvent loggingEvent){
            try {
                lock.lock();
                var builder = local.get();
                builder.current = pool[counter];
                builder.current.reset();
                var jsonLog = builder.current;
                // jsonLog.logger = ...
                jsonLog.level = loggingEvent.getLevel();
                jsonLog.throwable = loggingEvent.getThrowable();
                jsonLog.markers.addAll(loggingEvent.getMarkers());
                jsonLog.keyValuePairs.addAll(loggingEvent.getKeyValuePairs());
                jsonLog.arguments.addAll(loggingEvent.getArguments());

                counter = (counter + 1) & (pool.length - 1);

                return jsonLog;
            } finally {
                lock.unlock();
            }

        }
    }


    private JsonLogEventBuilder() {
        threadName = Thread.currentThread().getName();
    }

    @Override
    public LoggingEventBuilder setCause(Throwable cause) {
        this.current.throwable = cause;
        return this;
    }

    private ArrayList<Marker> getMarkers() {
        return this.current.markers;
    }
    @Override
    public LoggingEventBuilder addMarker(Marker marker) {
        getMarkers().add(marker);
        return this;
    }

    private ArrayList<Object> getArgumentsList() {
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

    private ArrayList<KeyValuePair> getKeyValueList() {
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
        this.current.logger.log(this.current);
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