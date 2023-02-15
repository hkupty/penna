package com.github.hkupty.maple.sink;

import com.github.hkupty.maple.models.JsonLog;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface Sink {

    interface SinkWriter {
        void writeString(String key, String value);
        void writeString( String value);
        void writeDouble(double value);
        void writeDouble(String key, double value);
        void writeInteger(String key, int value);
        void writeInteger(int value);
        void writeLong(String key, long value);
        void writeLong(long value);
        void writeBigDecimal(String key, BigDecimal value);
        void writeBigDecimal(BigDecimal value);
        void writeBigInteger(String key, BigInteger value);
        void writeBigInteger(BigInteger value);
        void writeAny(String key, Object value);
        void writeAny(Object value);
        void writeBoolean(String key, Boolean value);
        void writeBoolean(Boolean value);
        void startObject(String objectKey);
        void startObject();
        void startArray(String arrayKey);
        void startArray();
        void endArray();
        void endObject();

    }
    void render(JsonLog jsonLog) throws IOException;
}
