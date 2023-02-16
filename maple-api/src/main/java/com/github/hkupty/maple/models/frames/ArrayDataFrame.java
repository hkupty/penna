package com.github.hkupty.maple.models.frames;

import com.github.hkupty.maple.sink.Sink;

import java.util.Arrays;

public final class ArrayDataFrame<T> extends DataFrame<T[]> {
    public ArrayDataFrame(String key, T[] value) {
        super(key, value);
    }

    public void extend(T[] extra) {
        var next = Arrays.copyOf(value, value.length + extra.length);
        System.arraycopy(extra, 0, next, value.length, extra.length);
        this.value = next;
    }

    @Override
    public void write(Sink.SinkWriter writer) {
        writer.startArray(key);
        for (int i = 0; i < value.length; i ++) {
            writer.writeAny(value[i]);
        }
        writer.endArray();
    }
}
