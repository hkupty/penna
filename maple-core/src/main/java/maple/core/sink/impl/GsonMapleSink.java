package maple.core.sink.impl;

import com.google.gson.stream.JsonWriter;
import maple.api.models.LogField;
import maple.core.internals.Clock;
import maple.core.models.MapleLogEvent;
import maple.core.sink.SinkImpl;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class GsonMapleSink implements SinkImpl {
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
     * function signature `MapleLogEvent -> ()` that throws an exception without using generics.
     * This, in itself, is not a huge advantage, but allows us to go for a very straightforward
     * approach when writing the log messages, by picking the fields we want to write from the message
     * based on {@link MapleLogEvent#fieldsToLog} and mapping to the appropriate function.
     * <br />
     * In {@link GsonMapleSink}, it is defined as a local reference to each emit* method in an
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
        void apply(MapleLogEvent event) throws IOException;
    }

    private final Emitter[] emitters;
    private static final int MAX_STACK_DEPTH = 15;

    private final AtomicLong counter = new AtomicLong(0L);

    private JsonWriter jsonWriter;
    private Writer writer;


    public GsonMapleSink() {
        // WARNING! Introducing new log fields requires this array to be updated.
        emitters = new Emitter[LogField.values().length];
        emitters[LogField.Counter.ordinal()] = this::emitCounter;
        emitters[LogField.Timestamp.ordinal()] = this::emitTimestamp;
        emitters[LogField.Level.ordinal()] = this::emitLevel;
        emitters[LogField.Message.ordinal()] = this::emitMessage;
        emitters[LogField.LoggerName.ordinal()] = this::emitLogger;
        emitters[LogField.ThreadName.ordinal()] = this::emitThreadName;
        emitters[LogField.MDC.ordinal()] = this::emitMDC;
        emitters[LogField.Markers.ordinal()] = this::emitMarkers;
        emitters[LogField.Throwable.ordinal()] = this::emitThrowable;
        emitters[LogField.KeyValuePairs.ordinal()] = this::emitKeyValuePair;
        emitters[LogField.Extra.ordinal()] = this::emitExtra;
    }

    @Override
    public void init(Writer writer) throws IOException {
        this.writer = writer;
        jsonWriter = new JsonWriter(writer);
        jsonWriter.setLenient(true);
        jsonWriter.setIndent("");
    }

    private void writeThrowable(Throwable throwable) throws IOException {
        String message;
        StackTraceElement[] frames;
        Throwable cause;

        if((message = throwable.getMessage()) != null) {
            jsonWriter.name("message").value(message);
        }

        if ((frames = throwable.getStackTrace()) != null) {
            jsonWriter.name("stacktrace").beginArray();
            for (int index = 0; index < Math.min(frames.length, MAX_STACK_DEPTH); index++) {
                jsonWriter.value(frames[index].toString());
            }

            if (frames.length > MAX_STACK_DEPTH) {
                jsonWriter.value("...");
            }
            jsonWriter.endArray();
        }

        if ((cause = throwable.getCause()) != null) {
            jsonWriter.name("cause").beginObject();
            writeThrowable(cause);
            jsonWriter.endObject();
        }
    }

    private void writeMap(Map map) throws IOException {
        jsonWriter.beginObject();
        for (var key : map.keySet()) {
            jsonWriter.name(key.toString());
            writeObject(map.get(key));
        }
        jsonWriter.endObject();
    }

    private void writeArray(List lst) throws IOException {
        jsonWriter.beginArray();
        for(var value : lst){
            writeObject(value);
        }
        jsonWriter.endArray();
    }

    private void writeArray(Object... lst) throws IOException {
        jsonWriter.beginArray();
        for (var value : lst) {
            writeObject(value);
        }
        jsonWriter.endArray();
    }

    private void writeObject(Object object) throws IOException {
        if (object instanceof Throwable throwable) {
            writeThrowable(throwable);
        } else if (object instanceof Map map) {
            writeMap(map);
        } else if (object instanceof List lst) {
            writeArray(lst);
        } else if (object instanceof Object[] lst) {
            writeArray(lst);
        } else {
            if (object == null) {
                jsonWriter.nullValue();
            } else if (object instanceof String s) {
                jsonWriter.value(s);
            } else if (object instanceof Number n) {
                jsonWriter.value(n);
            } else if (object instanceof Boolean b) {
                jsonWriter.value(b);
            } else {
                jsonWriter.value(object.toString());
            }
        }
    }

    private void emitMessage(MapleLogEvent logEvent) throws IOException {
        jsonWriter.name(LogField.Message.fieldName).value(logEvent.message);
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitTimestamp(MapleLogEvent logEvent) throws IOException {
        jsonWriter.name(LogField.Timestamp.fieldName).value(Clock.getTimestamp());
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitMDC(MapleLogEvent logEvent) throws IOException {
        var mdc = MDC.getCopyOfContextMap();
        if (mdc != null) {
            jsonWriter.name(LogField.MDC.fieldName).beginObject();
            for (var kv : mdc.entrySet()) {
                jsonWriter.name(kv.getKey()).value(kv.getValue());
            }
            jsonWriter.endObject();
        }
    }

    private void emitLogger(MapleLogEvent logEvent) throws IOException {
        jsonWriter.name(LogField.LoggerName.fieldName).value(logEvent.getLoggerName());
    }

    private void emitLevel(MapleLogEvent logEvent) throws IOException {
        jsonWriter.name(LogField.Level.fieldName).value(LEVEL_MAPPING[logEvent.level.ordinal()]);
    }

    private void emitThreadName(MapleLogEvent logEvent) throws IOException {
        jsonWriter.name(LogField.ThreadName.fieldName).value(logEvent.getThreadName());
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitCounter(MapleLogEvent logEvent) throws IOException {
        jsonWriter.name(LogField.Counter.fieldName).value(counter.getAndIncrement());
    }

    private void emitMarkers(MapleLogEvent logEvent) throws IOException {
        if (!logEvent.markers.isEmpty()) {
            jsonWriter.name(LogField.Markers.fieldName).beginArray();
            for (int i = 0; i < logEvent.markers.size(); i++) {
                jsonWriter.value(logEvent.markers.get(i).getName());
            }
            jsonWriter.endArray();
        }
    }

    private void emitThrowable(MapleLogEvent logEvent) throws IOException {
        if (logEvent.throwable != null) {
            jsonWriter.name(LogField.Throwable.fieldName).endObject();
            writeThrowable(logEvent.throwable);
            jsonWriter.endObject();
        }
    }

    private void emitKeyValuePair(MapleLogEvent logEvent) throws IOException {
        if (!logEvent.keyValuePairs.isEmpty()) {
            jsonWriter.name(LogField.KeyValuePairs.fieldName).beginObject();
            for (int i = 0; i < logEvent.keyValuePairs.size(); i++) {
                var kvp = logEvent.keyValuePairs.get(i);
                jsonWriter.name(kvp.key);
                writeObject(kvp.value);
            }
            jsonWriter.endObject();
        }
    }

    private void emitExtra (MapleLogEvent logEvent) throws IOException {
        if (logEvent.extra != null) {
            jsonWriter.name(LogField.Extra.fieldName).beginObject();
            jsonWriter.endObject();
        }
    }

    @Override
    public void write(MapleLogEvent logEvent) throws IOException {
        jsonWriter.beginObject();
        for (int i = 0; i < logEvent.fieldsToLog.length; i++){
            emitters[logEvent.fieldsToLog[i].ordinal()].apply(logEvent);
        }
        jsonWriter.endObject();
        writer.write(LINE_BREAK);
        jsonWriter.flush();
    }
}
