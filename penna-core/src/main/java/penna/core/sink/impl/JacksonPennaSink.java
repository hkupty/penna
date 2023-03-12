package penna.core.sink.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import penna.api.models.LogField;
import penna.core.internals.Clock;
import penna.core.models.PennaLogEvent;
import penna.core.sink.SinkImpl;
import penna.core.sink.impl.jackson.NOPPrettyPrinter;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class JacksonPennaSink implements SinkImpl {
    private static final String LINE_BREAK = System.getProperty("line.separator");
    private static final String[] LEVEL_MAPPING = new String[5];

    static {
        LEVEL_MAPPING[Level.TRACE.ordinal()] = "TRACE";
        LEVEL_MAPPING[Level.DEBUG.ordinal()] = "DEBUG";
        LEVEL_MAPPING[Level.INFO.ordinal()] = "INFO";
        LEVEL_MAPPING[Level.WARN.ordinal()] = "WARN";
        LEVEL_MAPPING[Level.ERROR.ordinal()] = "ERROR";
    }

    /**
     * The Emitter functional interface allows us to define the specific
     * function signature `PennaLogEvent -> ()` that throws an exception without using generics.
     * This, in itself, is not a huge advantage, but allows us to go for a very straightforward
     * approach when writing the log messages, by picking the fields we want to write from the message
     * based on {@link PennaLogEvent#fieldsToLog} and mapping to the appropriate function.
     * <br />
     * In {@link JacksonPennaSink}, it is defined as a local reference to each emit* method in an
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
    private static final int MAX_STACK_DEPTH = 15;

    private final AtomicLong counter = new AtomicLong(0L);

    private JsonGenerator jsonGenerator;



    public JacksonPennaSink() {
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

    @Override
    public void init(final Writer writer) throws IOException {
        JsonFactory factory = JsonFactory.builder()
                .enable(JsonWriteFeature.ESCAPE_NON_ASCII)
                .build();

        jsonGenerator = factory.createGenerator(writer);
        jsonGenerator.setPrettyPrinter(NOPPrettyPrinter.getInstance());
        jsonGenerator.enable(Feature.FLUSH_PASSED_TO_STREAM);
        jsonGenerator.disable(Feature.AUTO_CLOSE_TARGET);
        jsonGenerator.disable(Feature.STRICT_DUPLICATE_DETECTION);
        jsonGenerator.disable(Feature.AUTO_CLOSE_JSON_CONTENT);
        // Initialize with an empty line break
        jsonGenerator.writeRaw(LINE_BREAK);
    }

    private void writeThrowable(final Throwable throwable) throws IOException {
        final String message;
        StackTraceElement[] frames;
        Throwable cause;

        if((message = throwable.getMessage()) != null) {
            jsonGenerator.writeStringField("message", message);
        }

        if ((frames = throwable.getStackTrace()) != null) {
            jsonGenerator.writeArrayFieldStart("stacktrace");
            for (int index = 0; index < Math.min(frames.length, MAX_STACK_DEPTH); index++) {
                jsonGenerator.writeString(frames[index].toString());
            }

            if (frames.length > MAX_STACK_DEPTH) {
                jsonGenerator.writeString("...");
            }
            jsonGenerator.writeEndArray();
        }

        if ((cause = throwable.getCause()) != null) {
            jsonGenerator.writeObjectFieldStart("cause");
            writeThrowable(cause);
            jsonGenerator.writeEndObject();
        }
    }

    private void writeMap(final Map map) throws IOException {
        jsonGenerator.writeStartObject();
        for (var key : map.keySet()) {
            jsonGenerator.writeFieldName(key.toString());
            writeObject(map.get(key));
        }
        jsonGenerator.writeEndObject();
    }

    private void writeArray(final List lst) throws IOException {
        jsonGenerator.writeStartArray();
        for(var value : lst){
            writeObject(value);
        }
        jsonGenerator.writeEndArray();
    }

    private void writeArray(final Object... lst) throws IOException {
        jsonGenerator.writeStartArray();
        for (var value : lst) {
            writeObject(value);
        }
        jsonGenerator.writeEndArray();
    }

    private void writeObject(final Object object) throws IOException {
        if (object instanceof Throwable throwable) {
            writeThrowable(throwable);
        } else if (object instanceof Map map) {
            writeMap(map);
        } else if (object instanceof List lst) {
            writeArray(lst);
        } else if (object instanceof Object[] lst) {
            writeArray(lst);
        } else {
            jsonGenerator.writeObject(object);
        }
    }

    private void emitMessage(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.writeStringField(LogField.MESSAGE.fieldName, logEvent.message);
    }


    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitTimestamp(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.writeNumberField(LogField.TIMESTAMP.fieldName, Clock.getTimestamp());
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitMDC(final PennaLogEvent logEvent) throws IOException {
        var mdc = MDC.getCopyOfContextMap();
        if (mdc != null) {
            jsonGenerator.writeObjectFieldStart(LogField.MDC.fieldName);
            for (var kv : mdc.entrySet()) {
                jsonGenerator.writeStringField(kv.getKey(), kv.getValue());
            }
            jsonGenerator.writeEndObject();
        }
    }

    private void emitLogger(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.writeStringField(LogField.LOGGER_NAME.fieldName, logEvent.getLoggerName());
    }

    private void emitLevel(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.writeStringField(LogField.LEVEL.fieldName, LEVEL_MAPPING[logEvent.level.ordinal()]);
    }

    private void emitThreadName(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.writeStringField(LogField.THREAD_NAME.fieldName, logEvent.getThreadName());
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitCounter(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.writeNumberField(LogField.COUNTER.fieldName, counter.getAndIncrement());
    }

    private void emitMarkers(final PennaLogEvent logEvent) throws IOException {
        if (!logEvent.markers.isEmpty()) {
            jsonGenerator.writeArrayFieldStart(LogField.MARKERS.fieldName);
            for (int i = 0; i < logEvent.markers.size(); i++) {
                jsonGenerator.writeString(logEvent.markers.get(i).getName());
            }
            jsonGenerator.writeEndArray();
        }
    }

    private void emitThrowable(final PennaLogEvent logEvent) throws IOException {
        if (logEvent.throwable != null) {
            jsonGenerator.writeObjectFieldStart(LogField.THROWABLE.fieldName);
            writeThrowable(logEvent.throwable);
            jsonGenerator.writeEndObject();
        }
    }

    private void emitKeyValuePair(final PennaLogEvent logEvent) throws IOException {
        if (!logEvent.keyValuePairs.isEmpty()) {
            jsonGenerator.writeObjectFieldStart(LogField.KEY_VALUE_PAIRS.fieldName);
            for (int i = 0; i < logEvent.keyValuePairs.size(); i++) {
                var kvp = logEvent.keyValuePairs.get(i);
                jsonGenerator.writeFieldName(kvp.key);
                writeObject(kvp.value);
            }
            jsonGenerator.writeEndObject();
        }
    }

    private void emitExtra (final PennaLogEvent logEvent) throws IOException {
        if (logEvent.extra != null) {
            jsonGenerator.writeObjectFieldStart(LogField.THROWABLE.fieldName);
            writeObject(logEvent.throwable);
            jsonGenerator.writeEndObject();
        }
    }

    @Override
    public void write(final PennaLogEvent logEvent) throws IOException {
        jsonGenerator.writeStartObject();
        for (int i = 0; i < logEvent.fieldsToLog.length; i++){
            emitters[logEvent.fieldsToLog[i].ordinal()].apply(logEvent);
        }
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
    }
}
