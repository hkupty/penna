package maple.core.sink.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import maple.api.models.LogField;
import maple.api.models.MapleLogEvent;
import maple.core.sink.SinkImpl;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class JacksonMapleSink implements SinkImpl {
    private static final EnumMap<Level, String> levelMapping = new EnumMap<>(Level.class);
    private static final int MAX_STACK_DEPTH = 15;

    static {
        levelMapping.put(Level.TRACE, "trace");
        levelMapping.put(Level.DEBUG, "debug");
        levelMapping.put(Level.INFO, "info");
        levelMapping.put(Level.WARN, "warn");
        levelMapping.put(Level.ERROR, "error");
    }
    private AtomicLong counter = new AtomicLong(0L);
    private JsonGenerator jsonGenerator;

    public JacksonMapleSink() {}

    @Override
    public void init(OutputStream os) throws IOException {
        var factory = JsonFactory.builder()
                .enable(JsonWriteFeature.ESCAPE_NON_ASCII)
                .build();
        jsonGenerator = factory.createGenerator(os);
        jsonGenerator.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        jsonGenerator.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        jsonGenerator.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
    }

    private void writeThrowable(Throwable throwable) throws IOException {
        String message;
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

    private void writeMap(Map map) throws IOException {
        jsonGenerator.writeStartObject();
        for (var key : map.keySet()) {
            jsonGenerator.writeFieldName(key.toString());
            writeObject(map.get(key));
        }
        jsonGenerator.writeEndObject();
    }

    private void writeArray(List lst) throws IOException {
        jsonGenerator.writeStartArray();
        for(var value : lst){
            writeObject(value);
        }
        jsonGenerator.writeEndArray();
    }

    private void writeArray(Object[] lst) throws IOException {
        jsonGenerator.writeStartArray();
        for (var value : lst) {
            writeObject(value);
        }
        jsonGenerator.writeEndArray();
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
            jsonGenerator.writeObject(object);
        }
    }

    private void emitMessage(MapleLogEvent logEvent) throws IOException {
        jsonGenerator.writeStringField(LogField.Message.fieldName, logEvent.message);
    }


    private void emitTimestamp(MapleLogEvent logEvent) throws IOException {
        jsonGenerator.writeNumberField(LogField.Timestamp.fieldName, System.currentTimeMillis());
    }

    private void emitMDC(MapleLogEvent logEvent) throws IOException {
        var mdc = MDC.getCopyOfContextMap();
        if (mdc != null) {
            jsonGenerator.writeObjectFieldStart(LogField.MDC.fieldName);
            for (var kv : mdc.entrySet()) {
                jsonGenerator.writeStringField(kv.getKey(), kv.getValue());
            }
            jsonGenerator.writeEndObject();
        }
    }

    private void emitLogger(MapleLogEvent logEvent) throws IOException {
        jsonGenerator.writeStringField(LogField.LoggerName.fieldName, logEvent.getLoggerName());
    }

    private void emitLevel(MapleLogEvent logEvent) throws IOException {
        jsonGenerator.writeStringField(LogField.Level.fieldName, levelMapping.get(logEvent.level));
    }

    private void emitThreadName(MapleLogEvent logEvent) throws IOException {
        jsonGenerator.writeStringField(LogField.ThreadName.fieldName, logEvent.getThreadName());
    }

    private void emitCounter(MapleLogEvent logEvent) throws IOException {
        jsonGenerator.writeNumberField(LogField.Counter.fieldName, counter.getAndIncrement());
    }

    private void emitMarkers(MapleLogEvent logEvent) throws IOException {
        if (!logEvent.markers.isEmpty()) {
            jsonGenerator.writeArrayFieldStart(LogField.Markers.fieldName);
            for (int i = 0; i < logEvent.markers.size(); i++) {
                jsonGenerator.writeString(logEvent.markers.get(i).getName());
            }
            jsonGenerator.writeEndArray();
        }
    }

    private void emitThrowable(MapleLogEvent logEvent) throws IOException {
        if (logEvent.throwable != null) {
            jsonGenerator.writeObjectFieldStart(LogField.Throwable.fieldName);
            writeThrowable(logEvent.throwable);
            jsonGenerator.writeEndObject();
        }
    }

    private void emitKeyValuePair(MapleLogEvent logEvent) throws IOException {
        if (!logEvent.keyValuePairs.isEmpty()) {
            jsonGenerator.writeObjectFieldStart(LogField.KeyValuePairs.fieldName);
            for (int i = 0; i < logEvent.keyValuePairs.size(); i++) {
                var kvp = logEvent.keyValuePairs.get(i);
                jsonGenerator.writeFieldName(kvp.key);
                writeObject(kvp.value);
            }
            jsonGenerator.writeEndObject();
        }
    }

    private void emitExtra (MapleLogEvent logEvent) throws IOException {
        if (logEvent.extra != null) {
            jsonGenerator.writeObjectFieldStart(LogField.Throwable.fieldName);
            writeObject(logEvent.throwable);
            jsonGenerator.writeEndObject();
        }
    }


    @Override
    public void write(MapleLogEvent logEvent) throws IOException {
        jsonGenerator.writeStartObject();
        for (int i = 0; i < logEvent.fieldsToLog.length; i++){
            switch (logEvent.fieldsToLog[i]){
                case Level -> emitLevel(logEvent);
                case Counter -> emitCounter(logEvent);
                case LoggerName -> emitLogger(logEvent);
                case Message -> emitMessage(logEvent);
                case Markers -> emitMarkers(logEvent);
                case KeyValuePairs -> emitKeyValuePair(logEvent);
                case ThreadName -> emitThreadName(logEvent);
                case Timestamp -> emitTimestamp(logEvent);
                case Throwable -> emitThrowable(logEvent);
                case MDC -> emitMDC(logEvent);
                case Extra -> emitExtra(logEvent);
            }
        }
        jsonGenerator.writeEndObject();
        jsonGenerator.writeRaw('\n');
        jsonGenerator.flush();
    }
}
