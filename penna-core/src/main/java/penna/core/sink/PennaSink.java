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
import java.util.function.BiConsumer;

public final class PennaSink implements SinkImpl, Closeable {
    private final int[] filterHashes = new int[StackTraceBloomFilter.NUMBER_OF_HASHES];

    private final AtomicLong counter = new AtomicLong(0L);

    private DirectJson jsonGenerator;

    private final PennaMDCAdapter mdcAdapter;
    private BiConsumer<String, String> mdcWriter;



    // From the same ticket that PMD references, https://bugs.openjdk.org/browse/JDK-8080225, it is noted that
    // in JDK 10 the problem was solved. We are targeting JDK 17+, so the problem won't affect us.
    // Plus, any other alternative is significantly slower.
    @SuppressWarnings("PMD.AvoidFileStream")
    public PennaSink() {
        if (MDC.getMDCAdapter() instanceof PennaMDCAdapter adapter) {
            mdcAdapter = adapter;
        } else {
            MiniLogger.error("Not using PennaMDCAdapter for some reason! MDC will be off");
            mdcAdapter = null;
        }

    }

    public static SinkImpl getSink() {
        PennaSink sinkImpl = new PennaSink();
        sinkImpl.init(OutputManager.Impl.get().getChannel());
        return sinkImpl;
    }

    @Override
    public void init(final FileChannel channel) {
        jsonGenerator = new DirectJson(channel);
        mdcWriter = jsonGenerator::writeStringValue;
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

    private void writeThrowable(final Throwable throwable, LogConfig config, int level) {
        final String message;
        StackTraceElement[] frames;
        Throwable cause;

        jsonGenerator.writeStringValue("class", throwable.getClass().getName());

        if((message = throwable.getMessage()) != null) {
            jsonGenerator.writeStringValue("message", message);
        }

        if ((frames = throwable.getStackTrace()) != null && frames.length > 0) {
            jsonGenerator.writeKeyString("stacktrace");
            jsonGenerator.openArray();
            var brokenOut = false;
            var filter = config.filter();
            for (int index = 0; index < Math.min(frames.length, config.stacktraceDepth()); index++) {
                filter.hash(filterHashes, frames[index]);
                writeStackFrame(frames[index]);
                jsonGenerator.writeRaw(',');
                if (filter.check(filterHashes)) {
                    jsonGenerator.writeUnsafeString("... repeated frames omitted");
                    brokenOut = true;
                    break;
                }
                filter.mark(filterHashes);
            }

            if (!brokenOut && frames.length > config.stacktraceDepth()) {
                jsonGenerator.writeUnsafeString("...");
            }

            jsonGenerator.closeArray();
            jsonGenerator.writeSep();
        }

        if (++level < config.traverseDepth() && throwable.getSuppressed().length > 0) {
            jsonGenerator.openArray("suppressed");
            var suppressed = throwable.getSuppressed();
            for (int i = 0; i < suppressed.length; i++) {
                jsonGenerator.openObject();
                writeThrowable(suppressed[i], config, level);
                jsonGenerator.closeObject();
                jsonGenerator.writeSep();
            }
            jsonGenerator.closeArray();
            jsonGenerator.writeSep();
            --level;
        }

        if (++level < config.traverseDepth() && (cause = throwable.getCause()) != null) {
            jsonGenerator.writeKeyString("cause");
            jsonGenerator.openObject();
            writeThrowable(cause, config, level);
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
            writeThrowable(throwable, config, 0);
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
        jsonGenerator.writeStringValueFormatting(LogField.MESSAGE.fieldName, logEvent.message, logEvent.arguments);
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitTimestamp(final PennaLogEvent logEvent) {
        jsonGenerator.writeNumberValue(LogField.TIMESTAMP.fieldName, Clock.getTimestamp());
    }


    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitMDC(final PennaLogEvent logEvent) {
        if (mdcAdapter.isNotEmpty()) {
            jsonGenerator.openObject(LogField.MDC.fieldName);
            mdcAdapter.forEach(mdcWriter);
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    private void emitLogger(final PennaLogEvent logEvent) {
        jsonGenerator.writeStringValue(LogField.LOGGER_NAME.fieldName, logEvent.getLoggerName());
    }

    private void emitLevel(final PennaLogEvent logEvent) {
        jsonGenerator.writeKeyString(LogField.LEVEL.fieldName);
        jsonGenerator.writeUnsafeString(logEvent.level.toString());
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
            writeThrowable(logEvent.throwable, logEvent.config, 0);
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
            switch (fields[i]){
                case LEVEL -> emitLevel(logEvent);
                case COUNTER -> emitCounter(logEvent);
                case LOGGER_NAME -> emitLogger(logEvent);
                case MESSAGE -> emitMessage(logEvent);
                case MARKERS -> emitMarkers(logEvent);
                case KEY_VALUE_PAIRS -> emitKeyValuePair(logEvent);
                case THREAD_NAME -> emitThreadName(logEvent);
                case TIMESTAMP -> emitTimestamp(logEvent);
                case THROWABLE -> emitThrowable(logEvent);
                case MDC -> emitMDC(logEvent);
                case EXTRA -> emitExtra(logEvent);
            }
        }

        jsonGenerator.closeObject();
        jsonGenerator.flush();
    }
}