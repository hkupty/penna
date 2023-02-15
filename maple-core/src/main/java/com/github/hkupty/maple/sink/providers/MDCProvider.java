package com.github.hkupty.maple.sink.providers;

import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;

import java.io.IOException;
import java.util.Map;

public class MDCProvider implements LogFieldProvider {
    private transient final String mdcFieldName;

    public MDCProvider(String mdcFieldName) {
        this.mdcFieldName = mdcFieldName;
    }

    public MDCProvider() {
        this("data");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log){
        if (log.keyValuePairs().length > 0) {
            writer.startObject(mdcFieldName);
            for(Map.Entry<String, String> keyValue : log.mdc().entrySet()) {
                writer.writeString(keyValue.getKey(), keyValue.getValue());
            }
            writer.endObject();
        }

    }
}
