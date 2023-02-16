package com.github.hkupty.maple.sink;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.github.hkupty.maple.internals.ByteBufferOutputStream;
import com.github.hkupty.maple.minilog.MiniLogger;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.models.frames.DataFrame;
import com.github.hkupty.maple.sink.providers.LogFieldProvider;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.concurrent.locks.ReentrantLock;

public class JacksonSink implements Sink, Sink.SinkWriter {
    private static class JsonWriteException extends RuntimeException {
        JsonWriteException(String message, Throwable cause) { super(message, cause);}
    }

    private static final String ERROR_MSG = "Failed to write ";
    private transient final JsonGenerator generator;
    private transient final ReentrantLock lock;
    private transient final ByteBuffer buffer;


    // TODO Move generator creation to out of constructor
    public JacksonSink(){

        buffer = ByteBuffer.allocateDirect(1024 * 1024);
        JsonFactory factory = JsonFactory.builder()
                .enable(StreamWriteFeature.USE_FAST_DOUBLE_WRITER)
                .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES)
                .build();
        try {
            generator = factory.createGenerator(new ByteBufferOutputStream(buffer));
        } catch (IOException ioe) {
            MiniLogger.error("Unable to create generator", ioe);
            throw new RuntimeException(ioe);
        }
        generator.disable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        lock = new ReentrantLock();
    }

    @Override
    public void render(DataFrame<?>[] dataFrames) {
        try {
        lock.lock();
        generator.writeStartObject();
        for (int i = 0; i < dataFrames.length; i++ ){
           dataFrames[i].write(this);
        }
        generator.writeEndObject();
        generator.writeRaw("\n");
        generator.flush();
        buffer.flip();
        var fc = SharedSinkLogic.getFileChannel();
        fc.write(buffer);
        buffer.clear();
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
    public void writeLong(String key, long value) {
        try {
            generator.writeNumberField(key, value);
        } catch (IOException e) {
            throw new JsonWriteException(ERROR_MSG + "long", e);
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
    public void startObject(String objectKey) {
        try {
            generator.writeFieldName(objectKey);
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