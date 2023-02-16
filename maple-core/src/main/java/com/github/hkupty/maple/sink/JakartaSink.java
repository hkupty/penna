package com.github.hkupty.maple.sink;

import com.github.hkupty.maple.models.frames.DataFrame;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;

import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;


public class JakartaSink implements Sink, Sink.SinkWriter{
    private transient final ReentrantLock lock;
    private transient final OutputStream channel;
    private transient final JsonGeneratorFactory factory;
    private transient JsonGenerator generator;

    public JakartaSink() {
        channel = SharedSinkLogic.getOutputStream();
        factory = Json.createGeneratorFactory(null);
        lock = new ReentrantLock();
    }

    @Override
    public void render(DataFrame<?>[] dataFrames) {
        try {
            lock.lock();
            generator = factory.createGenerator(channel);
            generator.writeStartObject();
            for (int i = 0; i < dataFrames.length; i++) {
                dataFrames[i].write(this);
            }
            generator.writeEnd();
            generator.flush();
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void writeString(String key, String value) {
        generator.write(key, value);
    }

    @Override
    public void writeString(String value) {
        generator.write(value);
    }

    @Override
    public void writeLong(String key, long value) {
        generator.write(key, value);
    }


    @Override
    public void writeAny(String key, Object value) {
        // TODO re-evaluate
        generator.write(key, value.toString());
    }

    @Override
    public void writeAny(Object value) {
        // TODO re-evaluate
        generator.write(value.toString());
    }

    @Override
    public void startObject(String objectKey) {
        generator.writeStartObject(objectKey);
    }

    @Override
    public void startArray(String arrayKey) {
        generator.writeStartArray(arrayKey);

    }

    @Override
    public void endArray() {
        generator.writeEnd();
    }

    @Override
    public void endObject() {
        generator.writeEnd();
    }
}
