package penna.core.sink.impl;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import penna.api.models.LogField;
import penna.core.internals.ByteBufferWriter;
import penna.core.internals.Clock;
import penna.core.models.PennaLogEvent;
import penna.core.sink.SinkImpl;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class JakartaPennaSink implements SinkImpl {
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
     * In {@link JakartaPennaSink}, it is defined as a local reference to each emit* method in an
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
        void apply(PennaLogEvent event) ;
    }

    private final Emitter[] emitters;
    private static final int MAX_STACK_DEPTH = 15;

    private final AtomicLong counter = new AtomicLong(0L);

    private JsonGenerator jsonGenerator;
    private JsonGeneratorFactory factory;
    private Writer writer;


    public JakartaPennaSink() {
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
    public void init(FileChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        this.writer = new ByteBufferWriter(buffer, channel);

        factory = Json.createGeneratorFactory(null);
    }

    private void newJsonGenerator(){
        jsonGenerator = factory.createGenerator(writer);
    }

    private void writeThrowable(Throwable throwable) {
        String message;
        StackTraceElement[] frames;
        Throwable cause;

        if((message = throwable.getMessage()) != null) {
            jsonGenerator.write("message", message);
        }

        if ((frames = throwable.getStackTrace()) != null) {
            jsonGenerator.writeStartArray("stacktrace");
            for (int index = 0; index < Math.min(frames.length, MAX_STACK_DEPTH); index++) {
                jsonGenerator.write(frames[index].toString());
            }

            if (frames.length > MAX_STACK_DEPTH) {
                jsonGenerator.write("...");
            }
            jsonGenerator.writeEnd();
        }

        if ((cause = throwable.getCause()) != null) {
            jsonGenerator.writeStartObject("cause");
            writeThrowable(cause);
            jsonGenerator.writeEnd();
        }
    }

    private void writeMap(Map map)  {
        jsonGenerator.writeStartObject();
        for (var key : map.keySet()) {
            jsonGenerator.write(key.toString());
            writeObject(map.get(key));
        }
        jsonGenerator.writeEnd();
    }

    private void writeArray(List lst)  {
        jsonGenerator.writeStartArray();
        for(var value : lst){
            writeObject(value);
        }
        jsonGenerator.writeEnd();
    }

    private void writeArray(Object... lst)  {
        jsonGenerator.writeStartArray();
        for (var value : lst) {
            writeObject(value);
        }
        jsonGenerator.writeEnd();
    }

    private void writeObject(Object object)  {
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
                jsonGenerator.writeNull();
            } else if (object instanceof String s) {
                jsonGenerator.write(s);
            } else if (object instanceof BigDecimal n) {
                jsonGenerator.write(n);
            } else if (object instanceof BigInteger n) {
                jsonGenerator.write(n);
            } else if (object instanceof Integer n) {
                jsonGenerator.write(n);
            } else if (object instanceof Long n) {
                jsonGenerator.write(n);
            } else if (object instanceof Double n) {
                jsonGenerator.write(n);
            } else if (object instanceof Boolean n) {
                jsonGenerator.write(n);
            } else {
                jsonGenerator.write(object.toString());
            }
        }
    }

    private void emitMessage(PennaLogEvent logEvent)  {
        jsonGenerator.write(LogField.MESSAGE.fieldName, logEvent.message);
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitTimestamp(PennaLogEvent logEvent)  {
        jsonGenerator.write(LogField.TIMESTAMP.fieldName, Clock.getTimestamp());
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitMDC(PennaLogEvent logEvent)  {
        var mdc = MDC.getCopyOfContextMap();
        if (mdc != null) {
            jsonGenerator.writeStartObject(LogField.MDC.fieldName);
            for (var kv : mdc.entrySet()) {
                jsonGenerator.write(kv.getKey(), kv.getValue());
            }
            jsonGenerator.writeEnd();
        }
    }

    private void emitLogger(PennaLogEvent logEvent)  {
        jsonGenerator.write(LogField.LOGGER_NAME.fieldName, logEvent.getLoggerName());
    }

    private void emitLevel(PennaLogEvent logEvent)  {
        jsonGenerator.write(LogField.LEVEL.fieldName, LEVEL_MAPPING[logEvent.level.ordinal()]);
    }

    private void emitThreadName(PennaLogEvent logEvent)  {
        jsonGenerator.write(LogField.THREAD_NAME.fieldName, logEvent.getThreadName());
    }

    // The method must conform to the functional interface, so we should ignore this rule here.
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void emitCounter(PennaLogEvent logEvent) {
        jsonGenerator.write(LogField.COUNTER.fieldName, counter.getAndIncrement());
    }

    private void emitMarkers(PennaLogEvent logEvent) {
        if (!logEvent.markers.isEmpty()) {
            jsonGenerator.writeStartArray(LogField.MARKERS.fieldName);
            for (int i = 0; i < logEvent.markers.size(); i++) {
                jsonGenerator.write(logEvent.markers.get(i).getName());
            }
            jsonGenerator.writeEnd();
        }
    }

    private void emitThrowable(PennaLogEvent logEvent)  {
        if (logEvent.throwable != null) {
            jsonGenerator.writeStartObject(LogField.THROWABLE.fieldName);
            writeThrowable(logEvent.throwable);
            jsonGenerator.writeEnd();
        }
    }

    private void emitKeyValuePair(PennaLogEvent logEvent)  {
        if (!logEvent.keyValuePairs.isEmpty()) {
            jsonGenerator.writeStartObject(LogField.KEY_VALUE_PAIRS.fieldName);
            for (int i = 0; i < logEvent.keyValuePairs.size(); i++) {
                var kvp = logEvent.keyValuePairs.get(i);
                jsonGenerator.write(kvp.key);
                writeObject(kvp.value);
            }
            jsonGenerator.writeEnd();
        }
    }

    private void emitExtra (PennaLogEvent logEvent)  {
        if (logEvent.extra != null) {
            jsonGenerator.write(LogField.THROWABLE.fieldName);
            writeObject(logEvent.throwable);
            jsonGenerator.writeEnd();
        }
    }

    @Override
    public void write(PennaLogEvent logEvent) throws IOException {
        newJsonGenerator();
        jsonGenerator.writeStartObject();
        for (int i = 0; i < logEvent.fieldsToLog.length; i++){
            emitters[logEvent.fieldsToLog[i].ordinal()].apply(logEvent);
        }
        jsonGenerator.writeEnd();
        writer.write(LINE_BREAK);
        jsonGenerator.flush();
    }
}
