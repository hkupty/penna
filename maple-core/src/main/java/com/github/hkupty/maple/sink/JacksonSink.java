package com.github.hkupty.maple.sink;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.github.hkupty.maple.minilog.MiniLogger;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.providers.LogFieldProvider;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.util.concurrent.locks.ReentrantLock;

public class JacksonSink implements Sink, Sink.SinkWriter {
    private static class JsonWriteException extends RuntimeException {
        JsonWriteException(String message, Throwable cause) { super(message, cause);}
    }

    private static final String ERROR_MSG = "Failed to write ";
    private transient final JsonGenerator generator;
    private transient final LogFieldProvider[] providers;
    private transient final ReentrantLock lock;

    // TODO Move generator creation to out of constructor
    public JacksonSink(LogFieldProvider[] providers){
        this.providers = providers;
        JsonFactory factory = JsonFactory.builder()
                .enable(StreamWriteFeature.USE_FAST_DOUBLE_WRITER)
                .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES)
                .build();
        try {
            generator = factory.createGenerator(SharedSinkLogic.getOutputStream());
            generator.disable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
            lock = new ReentrantLock();
        } catch (IOException ioe) {
            MiniLogger.error("Panic! STDOUT is dead", ioe);
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public void render(JsonLog jsonLog) {
        try {
            lock.lock();
            generator.writeStartObject();
            for (int i = 0; i < providers.length; i++ ){
                providers[i].logField(this, jsonLog);
            }
            generator.writeEndObject();
            generator.writeRaw("\n");
            generator.flush();
        } catch (IOException | JsonWriteException ex) {
            MiniLogger.error("Unable to log", ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void writeString(String key, String value) {
        try {
            generator.writeStringField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "string", e);
        }
    }

    @Override
    public void writeString(String value) {
        try {
            generator.writeString(value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "string", e);
        }
    }

    @Override
    public void writeDouble(double value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "double", e);
        }
    }

    @Override
    public void writeDouble(String key, double value) {
        try {
            generator.writeNumberField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "double", e);
        }
    }

    @Override
    public void writeInteger(String key, int value) {
        try {
            generator.writeNumberField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "integer", e);
        }
    }

    @Override
    public void writeInteger(int value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "integer", e);
        }
    }
    @Override
    public void writeLong(String key, long value) {
        try {
            generator.writeNumberField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "long", e);
        }
    }

    @Override
    public void writeLong(long value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "long", e);
        }
    }

    @Override
    public void writeBigDecimal(String key, BigDecimal value) {
        try {
            generator.writeNumberField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "big decimal", e);
        }
    }

    @Override
    public void writeBigDecimal(BigDecimal value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "big decimal", e);
        }
    }

    @Override
    public void writeBigInteger(String key, BigInteger value) {
        try {
            generator.writeNumberField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "big integer", e);
        }
    }

    @Override
    public void writeBigInteger(BigInteger value) {
        try {
            generator.writeNumber(value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "big integer", e);
        }
    }

    @Override
    public void writeAny(String key, Object value) {
        try {
            generator.writeObjectField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "object", e);
        }
    }

    @Override
    public void writeAny(Object value) {
        try {
            generator.writeObject(value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "object", e);
        }
    }

    @Override
    public void writeBoolean(String key, Boolean value) {
        try {
            generator.writeBooleanField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "boolean", e);
        }
    }

    @Override
    public void writeBoolean(Boolean value) {
        try {
            generator.writeBoolean(value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "boolean", e);
        }
    }

    @Override
    public void startObject(String objectKey) {
        try {
            generator.writeFieldName(objectKey);
            generator.writeStartObject();
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "object", e);
        }
    }

    @Override
    public void startObject() {
        try {
            generator.writeStartObject();
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "object", e);
        }
    }

    @Override
    public void startArray(String arrayKey) {
        try {
            generator.writeFieldName(arrayKey);
            generator.writeStartArray();
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "object", e);
        }
    }

    @Override
    public void startArray() {
        try {
            generator.writeStartArray();
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "array", e);
        }
    }

    @Override
    public void endArray() {
        try {
            generator.writeEndArray();
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "array", e);
        }
    }

    @Override
    public void endObject() {
        try {
            generator.writeEndObject();
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "object", e);
        }

    }
}