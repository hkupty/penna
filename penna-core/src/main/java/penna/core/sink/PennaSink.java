package penna.core.sink;

import org.slf4j.MDC;
import org.slf4j.event.Level;
import penna.api.models.LogField;
import penna.core.internals.*;
import penna.core.minilog.MiniLogger;
import penna.core.models.LogConfig;
import penna.core.models.PennaLogEvent;
import penna.core.slf4j.PennaMDCAdapter;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class PennaSink implements SinkImpl, Closeable {
    private static final String[] LEVEL_MAPPING = new String[5];

    static {
        LEVEL_MAPPING[Level.TRACE.ordinal()] = "TRACE";
        LEVEL_MAPPING[Level.DEBUG.ordinal()] = "DEBUG";
        LEVEL_MAPPING[Level.INFO.ordinal()] = "INFO";
        LEVEL_MAPPING[Level.WARN.ordinal()] = "WARN";
        LEVEL_MAPPING[Level.ERROR.ordinal()] = "ERROR";
    }
    private final int[] filterHashes = new int[StackTraceBloomFilter.NUMBER_OF_HASHES];

    /**
     * The Emitter functional interface allows us to define the specific
     * function signature `PennaLogEvent -> ()` that throws an exception without using generics.
     * This, in itself, is not a huge advantage, but allows us to go for a very straightforward
     * approach when writing the log messages, by picking the fields we want to write from the message
     * based on {@link PennaLogEvent#config}'s {@link penna.api.config.Config#fields()} and mapping to the appropriate function.
     * <br />
     * In {@link PennaSink}, it is defined as a local reference to each emit* method in an
     * array, where each method is in the same position in the mapping array as the
     * respective {@link LogField#ordinal()} for that log field.
     * <br />
     * In other words, we do a very fast lookup based on array position to determine which emit* methods to call,
     * in which order.
     * While it might get duplicated in other sinks, the compiler seems to like it better when
     * kept as a private interface, as more optimizations are possible.
     */
    @FunctionalInterface
    private interface Emitter {
        void apply(final PennaLogEvent event) throws IOException;
    }

    private final Emitter[] emitters;

    private final AtomicLong counter = new AtomicLong(0L);

    private DirectJson jsonGenerator;


    // From the same ticket that PMD references, https://bugs.openjdk.org/browse/JDK-8080225, it is noted that
    // in JDK 10 the problem was solved. We are targeting JDK 17+, so the problem won't affect us.
    // Plus, any other alternative is significantly slower.
    @SuppressWarnings("PMD.AvoidFileStream")
    public PennaSink() {

        // WARNING! Introducing new log fields requires this array to be updated.
        emitters = new Emitter[LogField.values().length];
        emitters[LogField.COUNTER.ordinal()] = this::emitCounter;
        emitters[LogField.TIMESTAMP.ordinal()] = this::emitTimestamp;
        emitters[LogField.LEVEL.ordinal()] = this::emitLevel;
        emitters[LogField.MESSAGE.ordinal()] = this::emitMessage;
        emitters[LogField.LOGGER_NAME.ordinal()] = this::emitLogger;
        emitters[LogField.THREAD_NAME.ordinal()] = this::emitThreadName;
        emitters[LogField.MDC.ordinal()] = this::emitMDC;
        emitters[LogField.MARKERS.ordinal()] = this::emitMarkers;
        emitters[LogField.THROWABLE.ordinal()] = this::emitThrowable;
        emitters[LogField.KEY_VALUE_PAIRS.ordinal()] = this::emitKeyValuePair;
        emitters[LogField.EXTRA.ordinal()] = this::emitExtra;
    }

    public static SinkImpl getSink() {
        PennaSink sinkImpl = new PennaSink();
        sinkImpl.init(OutputManager.Impl.get().getChannel());
        return sinkImpl;
    }

    @Override
    public void init(final FileChannel channel) {
        jsonGenerator = new DirectJson(channel);
    }

    @Override
    public void close() throws IOException {
        jsonGenerator.close();
    }
    // Hand-crafted based on from StackTraceElement::toString
    // ClassLoader is intentionally removed
    private void writeStackFrame(StackTraceElement frame) {
        String fileName;

        jsonGenerator.writeQuote();

        jsonGenerator.writeRaw(frame.getClassName());
        jsonGenerator.writeRaw('.');
        jsonGenerator.writeRaw(frame.getMethodName());
        jsonGenerator.writeRaw('(');

        if (frame.isNativeMethod()) {
            jsonGenerator.writeRaw("Native Method");
        } else if ((fileName = frame.getFileName()) != null && !fileName.isEmpty()) {
            jsonGenerator.writeRaw(fileName);
            if (frame.getLineNumber() > 0){
                jsonGenerator.writeRaw(':');
                jsonGenerator.writeNumberRaw(frame.getLineNumber());
            }
        } else {
            jsonGenerator.writeRaw("Unknown Source");
        }

        jsonGenerator.writeRaw(')');
        jsonGenerator.writeQuote();

    }

    private void writeThrowable(final Throwable throwable, LogConfig config) {
        final String message;
        StackTraceElement[] frames;
        Throwable cause;

        jsonGenerator.writeStringValue("class", throwable.getClass().getName());

        if((message = throwable.getMessage()) != null) {
            jsonGenerator.writeStringValue("message", message);
        }

        if ((frames = throwable.getStackTrace()) != null && frames.length > 0) {
            jsonGenerator.writeString("stacktrace");
            jsonGenerator.writeEntrySep();
            jsonGenerator.openArray();
            var brokenOut = false;
            var filter = config.filter();
            for (int index = 0; index < Math.min(frames.length, config.stacktraceDepth()); index++) {
                filter.hash(filterHashes, frames[index]);
                writeStackFrame(frames[index]);
                jsonGenerator.writeRaw(',');
                if (filter.check(filterHashes)) {
                    jsonGenerator.writeString("... repeated frames omitted");
                    brokenOut = true;
                    break;
                }
                filter.mark(filterHashes);
            }

            if (!brokenOut && frames.length > config.stacktraceDepth()) {
                jsonGenerator.writeString("...");
            }

            jsonGenerator.closeArray();
            jsonGenerator.writeSep();
        }

        if (throwable.getSuppressed().length > 0) {
            jsonGenerator.openArray("suppressed");
            var suppressed = throwable.getSuppressed();
            for (int i = 0; i < suppressed.length; i++) {
                jsonGenerator.openObject();
                writeThrowable(suppressed[i], config);
                jsonGenerator.closeObject();
                jsonGenerator.writeSep();
            }
            jsonGenerator.closeArray();
            jsonGenerator.writeSep();
        }

        if ((cause = throwable.getCause()) != null) {
            jsonGenerator.writeString("cause");
            jsonGenerator.writeEntrySep();
            jsonGenerator.openObject();
            writeThrowable(cause, config);
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    private void writeMap(LogConfig config, final Map map) throws IOException {
        jsonGenerator.openObject();
        for (var key : map.keySet()) {
            jsonGenerator.writeString(key.toString());
            jsonGenerator.writeEntrySep();
            writeObject(config, map.get(key));
            jsonGenerator.writeSep();
        }
        jsonGenerator.closeObject();
        jsonGenerator.writeSep();
    }

    private void writeArray(LogConfig config, final List lst) throws IOException {
        jsonGenerator.openArray();
        for (Object o : lst) {
            writeObject(config, o);
        }
        jsonGenerator.closeArray();
        jsonGenerator.writeSep();
    }

    private void writeArray(LogConfig config, final Object... lst) throws IOException {
        jsonGenerator.openArray();
        for (Object o : lst) {
            writeObject(config, o);
        }
        jsonGenerator.closeArray();
        jsonGenerator.writeSep();
    }

    private void writeObject(LogConfig config, final Object object) throws IOException {
        if (object instanceof Throwable throwable) {
            config.filter().reset();
            writeThrowable(throwable, config);
        } else if (object instanceof Map map) {
            writeMap(config, map);
        } else if (object instanceof List lst) {
            writeArray(config, lst);
        } else if (object instanceof Object[] lst) {
            writeArray(config, lst);
        } else if (object instanceof String str){
            jsonGenerator.writeString(str);
        } else if (object instanceof Long lng){
            jsonGenerator.writeNumber(lng);
        } else if (object instanceof Double dbl){
            jsonGenerator.writeNumber(dbl);
        } else {
            jsonGenerator.writeString(object.toString());
        }
    }

    private void emitMessage(final PennaLogEvent logEvent) {
        jsonGenerator.writeStringValue(LogField.MESSAGE.fieldName, logEvent.message);
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitTimestamp(final PennaLogEvent logEvent) {
        jsonGenerator.writeNumberValue(LogField.TIMESTAMP.fieldName, Clock.getTimestamp());
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitMDC(final PennaLogEvent logEvent) {
        var mdc = MDC.getCopyOfContextMap();
        if (mdc != null) {
            jsonGenerator.openObject(LogField.MDC.fieldName);
            for (var kv : mdc.entrySet()) {
                jsonGenerator.writeStringValue(kv.getKey(), kv.getValue());
            }
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    private void emitLogger(final PennaLogEvent logEvent) {
        jsonGenerator.writeStringValue(LogField.LOGGER_NAME.fieldName, logEvent.getLoggerName());
    }

    private void emitLevel(final PennaLogEvent logEvent) {
        jsonGenerator.writeStringValue(LogField.LEVEL.fieldName, LEVEL_MAPPING[logEvent.level.ordinal()]);
    }

    private void emitThreadName(final PennaLogEvent logEvent) {
        jsonGenerator.writeStringValue(LogField.THREAD_NAME.fieldName, logEvent.getThreadName());
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitCounter(final PennaLogEvent logEvent) {
        jsonGenerator.writeNumberValue(LogField.COUNTER.fieldName, counter.getAndIncrement());
    }

    private void emitMarkers(final PennaLogEvent logEvent) {
        if (!logEvent.markers.isEmpty()) {
            jsonGenerator.openArray(LogField.MARKERS.fieldName);
            for (int i = 0; i < logEvent.markers.size(); i++) {
                jsonGenerator.writeString(logEvent.markers.get(i).getName());
            }
            jsonGenerator.closeArray();
            jsonGenerator.writeSep();
        }
    }

    private void emitThrowable(final PennaLogEvent logEvent) {
        if (logEvent.throwable != null) {
            logEvent.config.filter().reset();
            jsonGenerator.openObject(LogField.THROWABLE.fieldName);
            writeThrowable(logEvent.throwable, logEvent.config);
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    private void emitKeyValuePair(final PennaLogEvent logEvent) throws IOException {
        if (!logEvent.keyValuePairs.isEmpty()) {
            jsonGenerator.openObject(LogField.KEY_VALUE_PAIRS.fieldName);
            for (int i = 0; i < logEvent.keyValuePairs.size(); i++) {
                var kvp = logEvent.keyValuePairs.get(i);
                jsonGenerator.writeString(kvp.key);
                jsonGenerator.writeEntrySep();
                writeObject(logEvent.config, kvp.value);
            }
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    private void emitExtra (final PennaLogEvent logEvent) throws IOException {
        if (logEvent.extra != null) {
            jsonGenerator.openObject(LogField.THROWABLE.fieldName);
            writeObject(logEvent.config, logEvent.throwable);
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    @Override
    public void write(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.openObject();

        // This should be safe to do here since this is thread local
        var fields = logEvent.config.fields();

        for (int i = 0; i < fields.length; i++){
            emitters[fields[i].ordinal()].apply(logEvent);
        }

        jsonGenerator.closeObject();
        jsonGenerator.flush();
    }
}