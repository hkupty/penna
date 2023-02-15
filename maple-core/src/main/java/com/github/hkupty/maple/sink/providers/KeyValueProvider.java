package com.github.hkupty.maple.sink.providers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;

public class KeyValueProvider implements LogFieldProvider {
    private transient final String keyValueFieldName;

    public KeyValueProvider(String keyValueFieldName) {
        this.keyValueFieldName = keyValueFieldName;
    }

    public KeyValueProvider() {
        this("data");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log){
        if (log.keyValuePairs().length > 0) {
            writer.startObject(keyValueFieldName);
            for(int i = 0; i < log.keyValuePairs().length ; i++) {
                var kvp = log.keyValuePairs()[i];
                writer.writeAny(kvp.key, kvp.value);
            }
            writer.endObject();
        }

    }
}
