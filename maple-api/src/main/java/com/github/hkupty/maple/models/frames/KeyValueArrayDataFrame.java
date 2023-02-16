package com.github.hkupty.maple.models.frames;

import com.github.hkupty.maple.sink.Sink;
import org.slf4j.event.KeyValuePair;

public final class KeyValueArrayDataFrame extends DataFrame<KeyValuePair[]> {
    public KeyValueArrayDataFrame(String key, KeyValuePair[] value) {
        super(key, value);
    }

    @Override
    public void write(Sink.SinkWriter writer) {
        writer.startObject(key);
        for (int i = 0; i < value.length; i++) {
            var kvp = value[i];
            writer.writeAny(kvp.key, kvp.value);
        }
        writer.endObject();
    }
}
