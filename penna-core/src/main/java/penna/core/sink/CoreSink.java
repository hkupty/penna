package penna.core.sink;

import org.slf4j.MDC;
import penna.api.models.LogField;
import penna.core.internals.DirectJson;
import penna.core.internals.StackTraceBloomFilter;
import penna.core.models.LogConfig;
import penna.core.models.PennaLogEvent;
import penna.core.slf4j.PennaMDCAdapter;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import static penna.api.audit.Logger.report;

public final class CoreSink implements Sink, Closeable {
    private static final byte[] SUPPRESSED = "suppressed".getBytes();
    private static final byte[] STACKTRACE = "stacktrace".getBytes();
    private static final byte[] CLASS = "class".getBytes();
    private static final byte[] MESSAGE = "message".getBytes();
    private static final byte[] REPEATED = "... repeated frames omitted".getBytes();
    private static final byte[] ELLIPSIS = "...".getBytes();
    private static final byte[] CAUSE = "cause".getBytes();
    private static final byte[] NATIVE = "Native Method".getBytes();
    private static final byte[] UNKNOWN = "Unknown Source".getBytes();

    private static final byte[][] LEVEL_ENUM_MAP = new byte[][]{
            "ERROR".getBytes(),
            "WARN".getBytes(),
            "INFO".getBytes(),
            "DEBUG".getBytes(),
            "TRACE".getBytes()
    };

    private final int[] filterHashes = new int[StackTraceBloomFilter.NUMBER_OF_HASHES];

    private final AtomicLong counter = new AtomicLong(0L);

    private FileOutputStream fos;
    private final DirectJson jsonGenerator;

    private final PennaMDCAdapter mdcAdapter;
    private final BiConsumer<String, String> mdcWriter;

    // From the same ticket that PMD references, https://bugs.openjdk.org/browse/JDK-8080225, it is noted that
    // in JDK 10 the problem was solved. We are targeting JDK 17+, so the problem won't affect us.
    // Plus, any other alternative is significantly slower.
    @SuppressWarnings("PMD.AvoidFileStream")
    public CoreSink() {
        this(new FileOutputStream(FileDescriptor.out));
    }

    public CoreSink(FileOutputStream fos) {
        this(fos.getChannel());
        this.fos = fos;
    }

    public CoreSink(WritableByteChannel channel) {
        if (MDC.getMDCAdapter() instanceof PennaMDCAdapter adapter) {
            mdcAdapter = adapter;
        } else {
            report("ERROR", "Not using PennaMDCAdapter for some reason! MDC will be off");
            mdcAdapter = null;
        }
        jsonGenerator = new DirectJson(channel);
        mdcWriter = jsonGenerator::writeStringValue;
    }

    public static Sink getSink() {
        return new CoreSink();
    }

    @Override
    public void close() throws IOException {
        jsonGenerator.close();
        if (fos != null) {
            fos.close();
        }
    }

    // Hand-crafted based on from StackTraceElement::toString
    // ClassLoader is intentionally removed
    private void writeStackFrame(StackTraceElement frame) {
        String fileName;

        jsonGenerator.writeQuote();

        jsonGenerator.writeUnsafe(frame.getClassName());
        jsonGenerator.writeRaw('.');
        jsonGenerator.writeUnsafe(frame.getMethodName());
        jsonGenerator.writeRaw('(');

        if ((fileName = frame.getFileName()) != null && !fileName.isEmpty()) {
            jsonGenerator.writeUnsafe(fileName);
            if (frame.getLineNumber() > 0) {
                jsonGenerator.writeRaw(':');
                jsonGenerator.writeNumberRaw(frame.getLineNumber());
            }
        } else if (frame.isNativeMethod()) {
            jsonGenerator.writeRaw(NATIVE);
        } else {
            jsonGenerator.writeRaw(UNKNOWN);
        }

        jsonGenerator.writeRaw(')');
        jsonGenerator.writeQuote();

    }

    private void writeThrowable(final Throwable throwable, LogConfig config, int initialLevel) {
        int level = initialLevel;
        final String message;
        StackTraceElement[] frames;
        Throwable cause;
        // Assume a throwable will take at least 200 characters
        jsonGenerator.checkSpace(200);

        jsonGenerator.writeKey(CLASS);
        var classname = throwable.getClass().getName();
        jsonGenerator.writeUnsafeString(classname);

        if ((message = throwable.getMessage()) != null) {
            jsonGenerator.writeKey(MESSAGE);
            jsonGenerator.writeString(message);
        }

        if ((frames = throwable.getStackTrace()) != null && frames.length > 0) {
            jsonGenerator.writeKey(STACKTRACE);
            jsonGenerator.openArray();
            var brokenOut = false;
            var filter = config.filter;
            for (int index = 0; index < Math.min(frames.length, config.stacktraceDepth); index++) {
                filter.hash(filterHashes, frames[index]);
                jsonGenerator.checkSpace(128);
                writeStackFrame(frames[index]);
                jsonGenerator.writeRaw(',');
                if (filter.check(filterHashes)) {
                    jsonGenerator.writeStringFromBytes(REPEATED);
                    brokenOut = true;
                    break;
                }
                filter.mark(filterHashes);
            }

            if (!brokenOut && frames.length > config.stacktraceDepth) {
                jsonGenerator.writeStringFromBytes(ELLIPSIS);
            }

            jsonGenerator.closeArray();
            jsonGenerator.writeSep();
        }

        if (++level < config.traverseDepth && throwable.getSuppressed().length > 0) {
            jsonGenerator.openArray(SUPPRESSED);
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

        if (++level < config.traverseDepth && (cause = throwable.getCause()) != null) {
            jsonGenerator.writeKey(CAUSE);
            jsonGenerator.openObject();
            writeThrowable(cause, config, level);
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    private void writeMap(LogConfig config, final Map<?,?> map) throws IOException {
        jsonGenerator.checkSpace(4);
        jsonGenerator.openObject();
        for (var key : map.keySet()) {
            var keystr = key.toString();
            jsonGenerator.checkSpace(keystr.length());
            jsonGenerator.writeString(keystr);
            jsonGenerator.writeEntrySep();
            writeObject(config, map.get(key));
            jsonGenerator.writeSep();
        }
        jsonGenerator.closeObject();
        jsonGenerator.writeSep();
    }

    private void writeArray(LogConfig config, final List<?> lst) throws IOException {
        jsonGenerator.openArray();
        for (Object o : lst) {
            writeObject(config, o);
        }
        jsonGenerator.closeArray();
        jsonGenerator.writeSep();
    }

    private void writeArray(LogConfig config, final Object... lst) throws IOException {
        jsonGenerator.checkSpace(3);
        jsonGenerator.openArray();
        for (Object o : lst) {
            writeObject(config, o);
        }
        jsonGenerator.closeArray();
        jsonGenerator.writeSep();
    }

    private void writeObject(LogConfig config, final Object object) throws IOException {

        switch (object) {
            case Throwable throwable -> {
                config.filter.reset();
                writeThrowable(throwable, config, 0);
            }
            case Map<?, ?> map -> writeMap(config, map);
            case List<?> lst -> writeArray(config, lst);
            case Object[] lst -> writeArray(config, lst);
            case String str -> {
                jsonGenerator.checkSpace(str.length());
                jsonGenerator.writeString(str);
            }
            case Long num -> {
                // Long.MIN_SIZE will yield 20 chars
                jsonGenerator.checkSpace(20);
                jsonGenerator.writeNumber(num);
            }
            case Integer num -> {
                jsonGenerator.checkSpace(11);
                jsonGenerator.writeNumber(num);
            }
            case Float num -> {
                jsonGenerator.checkSpace(32);
                jsonGenerator.writeNumber(num);
            }
            case Double num -> {
                jsonGenerator.checkSpace(64);
                jsonGenerator.writeNumber(num);
            }
            case null -> {
                jsonGenerator.writeNull();
            }
            default -> {
                var str = object.toString();
                jsonGenerator.checkSpace(str.length());
                jsonGenerator.writeString(str);
            }
        }
    }

    private void emitMessage(final PennaLogEvent logEvent) {
        jsonGenerator.checkSpace(20 + logEvent.message.length());
        jsonGenerator.writeKey(LogField.MESSAGE.fieldName);
        jsonGenerator.writeStringFormatting(logEvent.message, logEvent.arguments);
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitTimestamp(final PennaLogEvent logEvent) {
        jsonGenerator.checkSpace(25);
        jsonGenerator.writeKey(LogField.TIMESTAMP.fieldName);
        jsonGenerator.writeNumber(logEvent.timestamp);
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
        jsonGenerator.checkSpace(9 + logEvent.logger.length);
        jsonGenerator.writeKey(LogField.LOGGER_NAME.fieldName);
        jsonGenerator.writeStringFromBytes(logEvent.logger);
    }

    private void emitLevel(final PennaLogEvent logEvent) {
        jsonGenerator.checkSpace(10);
        jsonGenerator.writeKey(LogField.LEVEL.fieldName);
        jsonGenerator.writeStringFromBytes(LEVEL_ENUM_MAP[logEvent.level.ordinal()]);
    }

    private void emitThreadName(final PennaLogEvent logEvent) {
        jsonGenerator.checkSpace(12 + logEvent.threadName.length);
        jsonGenerator.writeKey(LogField.THREAD_NAME.fieldName);
        jsonGenerator.writeStringFromBytes(logEvent.threadName);
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitCounter(final PennaLogEvent logEvent) {
        jsonGenerator.checkSpace(64);
        jsonGenerator.writeKey(LogField.COUNTER.fieldName);
        jsonGenerator.writeNumber(counter.getAndIncrement());
    }

    private void emitMarkers(final PennaLogEvent logEvent) {
        if (!logEvent.markers.isEmpty()) {
            jsonGenerator.openArray(LogField.MARKERS.fieldName);
            for (int i = 0; i < logEvent.markers.size(); i++) {
                var marker = logEvent.markers.get(i).getName();
                jsonGenerator.checkSpace(4 + marker.length());
                jsonGenerator.writeString(marker);
            }
            jsonGenerator.closeArray();
            jsonGenerator.writeSep();
        }
    }

    private void emitThrowable(final PennaLogEvent logEvent) {
        if (logEvent.throwable != null) {
            logEvent.config.filter.reset();
            jsonGenerator.openObject(LogField.THROWABLE.fieldName);
            writeThrowable(logEvent.throwable, logEvent.config, 0);
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    private void emitKeyValuePair(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.checkSpace(16);
        if (!logEvent.keyValuePairs.isEmpty()) {
            jsonGenerator.openObject(LogField.KEY_VALUE_PAIRS.fieldName);
            for (int i = 0; i < logEvent.keyValuePairs.size(); i++) {
                var kvp = logEvent.keyValuePairs.get(i);
                jsonGenerator.checkSpace(kvp.key().length() + 4);
                jsonGenerator.writeString(kvp.key());
                jsonGenerator.writeEntrySep();
                writeObject(logEvent.config, kvp.value());
            }
            jsonGenerator.closeObject();
            jsonGenerator.writeSep();
        }
    }

    private void emitExtra(final PennaLogEvent logEvent) throws IOException {
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

        var fields = logEvent.config.fields;

        for (int i = 0; i < fields.length; i++) {
            switch (fields[i]) {
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