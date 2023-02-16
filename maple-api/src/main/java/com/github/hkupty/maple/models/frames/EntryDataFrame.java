package com.github.hkupty.maple.models.frames;

import com.github.hkupty.maple.sink.Sink;

import java.util.Map;

public final class EntryDataFrame extends DataFrame<Map.Entry<String, String>[]> {

    private static final Map.Entry[] reference = new Map.Entry[]{};

    public EntryDataFrame(String key, Map.Entry<String, String>[] value) {
        super(key, value);
    }

    public EntryDataFrame(String key, Map<String, String> value) {
        this(key, value.entrySet().toArray(reference));
    }

    @Override
    public void write(Sink.SinkWriter writer) {
        writer.startObject(key);
        for (int i = 0; i < value.length; i++) {
            var kvp = value[i];
            writer.writeAny(kvp.getKey(), kvp.getValue());
        }
        writer.endObject();

    }
}
