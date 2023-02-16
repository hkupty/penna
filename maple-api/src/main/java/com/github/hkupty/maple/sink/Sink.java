package com.github.hkupty.maple.sink;

import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.models.frames.DataFrame;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface Sink {

    interface SinkWriter {
        void writeString(String key, String value);
        void writeString( String value);
        void writeLong(String key, long value);
        void writeAny(String key, Object value);
        void writeAny(Object value);
        void startObject(String objectKey);
        void startArray(String arrayKey);
        void endArray();
        void endObject();

    }
    void render(DataFrame<?>[] dataFrames);
}
